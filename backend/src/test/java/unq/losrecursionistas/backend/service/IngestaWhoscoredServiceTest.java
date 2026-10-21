package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unq.losrecursionistas.backend.adapter.whoscored.ScrapingConfiguration;
import unq.losrecursionistas.backend.adapter.whoscored.ScrapingPort;
import unq.losrecursionistas.backend.adapter.whoscored.dto.AparicionWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.EquipoWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.JugadorWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.LigaWhoscoredDto;
import unq.losrecursionistas.backend.model.EjecucionIngesta;
import unq.losrecursionistas.backend.model.Equipo;
import unq.losrecursionistas.backend.model.EstadisticaPartido;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.model.Liga;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioCasoVinculacion;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEjecucionIngesta;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEquipo;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEstadisticaPartido;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioJugador;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioLiga;
import unq.losrecursionistas.backend.service.impl.IngestaWhoscoredServiceImpl;
import unq.losrecursionistas.backend.service.impl.MatchingWhoscoredService;
import unq.losrecursionistas.backend.service.interfaces.ResultadoIngesta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestaWhoscoredServiceTest {
    @Mock RepositorioEjecucionIngesta ejecuciones;
    @Mock RepositorioJugador jugadores;
    @Mock RepositorioCasoVinculacion casos;
    @Mock RepositorioEstadisticaPartido estadisticas;
    @Mock MatchingWhoscoredService matching;
    @Mock ScrapingPort scraping;
    @Mock RepositorioLiga ligas;
    @Mock RepositorioEquipo equipos;

    private ScrapingConfiguration configuracion;
    private IngestaWhoscoredServiceImpl servicio;

    @BeforeEach
    void setUp() {
        configuracion = new ScrapingConfiguration();
        configuracion.setTemporadaInicio(LocalDate.of(2025, 8, 1));
        configuracion.setTemporadaFin(LocalDate.of(2026, 6, 30));
        servicio = new IngestaWhoscoredServiceImpl(ejecuciones, jugadores, casos, estadisticas,
                matching, scraping, configuracion, ligas, equipos);
        lenient().when(ejecuciones.existeActiva()).thenReturn(false);
        lenient().when(ejecuciones.guardar(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void persisteSoloAparicionesDeTemporadaConMinutosYActualizaContadores() {
        Jugador jugador = Jugador.builder().id(7L).idWhoscored(99L).activo(true).build();
        when(jugadores.obtenerJugadoresActivos()).thenReturn(List.of(jugador));
        when(estadisticas.buscarPorJugadorYPartido(7L, 10L)).thenReturn(null);
        when(scraping.obtenerApariciones(99L)).thenReturn(List.of(
                new AparicionWhoscoredDto(10L, LocalDate.of(2025, 9, 1), "Liga", 1L,
                        2L, "Equipo", 3L, "Rival", "LOCAL", 90, BigDecimal.TEN,
                        Map.of("goals", 2, "keyPasses", 4)),
                new AparicionWhoscoredDto(11L, LocalDate.of(2025, 7, 1), "Liga", 1L,
                        2L, "Equipo", 3L, "Rival", "LOCAL", 90, null, Map.of()),
                new AparicionWhoscoredDto(12L, LocalDate.of(2025, 9, 2), "Liga", 1L,
                        2L, "Equipo", 3L, "Rival", "LOCAL", 0, null, Map.of())));

        EjecucionIngesta resultado = servicio.iniciar(EjecucionIngesta.Alcance.COMPLETA, null);

        assertThat(resultado.getPartidosInsertados()).isEqualTo(1);
        assertThat(resultado.getAnomalias()).isEqualTo(2);
        verify(estadisticas).upsert(any(EstadisticaPartido.class));
    }

    @Test
    void rechazaUnaNuevaEjecucionMientrasHayOtraActiva() {
        when(ejecuciones.existeActiva()).thenReturn(true);

        EjecucionIngesta resultado = servicio.iniciar(EjecucionIngesta.Alcance.JUGADOR, 7L);

        assertThat(resultado.getEstado()).isEqualTo(EjecucionIngesta.Estado.RECHAZADA);
        assertThat(resultado.getMotivoRechazo()).contains("activa");
        verifyNoInteractions(jugadores, scraping, estadisticas);
    }

    @Test
    void ingestaEstadisticasDeJugadorUsandoSuIdWhoscored() {
        Jugador jugador = Jugador.builder().id(7L).idWhoscored(99L).activo(true).build();
        when(jugadores.obtenerJugadorPorId(7L)).thenReturn(jugador);
        when(scraping.obtenerApariciones(99L)).thenReturn(List.of(
                new AparicionWhoscoredDto(10L, LocalDate.of(2025, 9, 1), "Liga", 1L,
                        2L, "Equipo", 3L, "Rival", "LOCAL", 90, BigDecimal.TEN,
                        Map.of("goals", 1))));
        when(estadisticas.buscarPorJugadorYPartido(7L, 10L)).thenReturn(null);

        ResultadoIngesta resultado = servicio.ingestarEstadisticasJugador(7L);

        assertThat(resultado.partidosInsertados()).isEqualTo(1);
        verify(scraping).obtenerApariciones(99L);
        verify(estadisticas).upsert(any(EstadisticaPartido.class));
        verify(scraping, never()).obtenerAparicionesTemporada(anyLong(), anyString());
    }

    @Test
    void ejecutaFaseDeVinculacionAntesDeEstadisticasEnIngestaCompleta() {
        Liga ligaLocal = new Liga("Premier League", "PL");
        ligaLocal.setId(1L);
        when(ligas.obtenerLigas()).thenReturn(List.of(ligaLocal));
        when(scraping.descubrirLigas()).thenReturn(List.of(new LigaWhoscoredDto(100L, "Premier League", "/pl", "2025/2026")));

        Equipo equipoLocal = Equipo.builder().nombre("Arsenal FC").liga(ligaLocal).build();
        equipoLocal.setId(10L);
        when(equipos.obtenerEquiposDeLiga(1L)).thenReturn(List.of(equipoLocal));
        when(scraping.descubrirEquipos(100L)).thenReturn(List.of(new EquipoWhoscoredDto(200L, "Arsenal", "/arsenal", 100L)));

        JugadorWhoscoredDto jugadorDescubierto = new JugadorWhoscoredDto(300L, "Bukayo Saka", "/saka", null, "England", 200L, "Arsenal", List.of("Delantero"));
        when(scraping.descubrirJugadores(200L)).thenReturn(List.of(jugadorDescubierto));

        Jugador jugadorLocal = Jugador.builder().id(50L).nombre("Bukayo Saka").activo(true).build();
        when(matching.vincular(jugadorDescubierto)).thenReturn(MatchingWhoscoredService.ResultadoMatching.vinculado(jugadorLocal));
        when(jugadores.obtenerJugadoresActivos()).thenReturn(List.of(jugadorLocal));

        EjecucionIngesta resultado = servicio.iniciar(EjecucionIngesta.Alcance.COMPLETA, null);

        assertThat(resultado.getJugadoresVinculados()).isEqualTo(1);
        assertThat(ligaLocal.getIdWhoscored()).isEqualTo(100L);
        assertThat(equipoLocal.getIdWhoscored()).isEqualTo(200L);
        verify(ligas).guardar(ligaLocal);
        verify(equipos).guardar(equipoLocal);
        verify(jugadores).guardar(jugadorLocal);
    }

    // ── Tests para el flujo de procesarJugadorTemporada (ingestarEstadisticasJugador) ──

    /**
     * Cuando obtenerApariciones devuelve vacío, debe intentar obtenerAparicionesTemporada
     * usando el slug del jugador (extraído desde el campo slug almacenado).
     */
    @Test
    void usaFallbackAObtenerAparicionesTemporadaCuandoObtenerAparicionesEstaVacio() {
        Jugador jugador = Jugador.builder()
                .id(7L)
                .idWhoscored(99L)
                .slug("/players/99/show/lionel-messi")
                .activo(true)
                .build();
        when(jugadores.obtenerJugadorPorId(7L)).thenReturn(jugador);
        when(scraping.obtenerApariciones(99L)).thenReturn(List.of());
        when(scraping.obtenerAparicionesTemporada(99L, "lionel-messi")).thenReturn(List.of(
                new AparicionWhoscoredDto(20L, LocalDate.of(2025, 10, 5), "Liga", 1L,
                        5L, "Inter Miami", 6L, "Rival", "LOCAL", 85, new BigDecimal("7.5"),
                        Map.of("goals", 1))));
        when(estadisticas.buscarPorJugadorYPartido(7L, 20L)).thenReturn(null);

        ResultadoIngesta resultado = servicio.ingestarEstadisticasJugador(7L);

        assertThat(resultado.partidosInsertados()).isEqualTo(1);
        verify(scraping).obtenerApariciones(99L);
        verify(scraping).obtenerAparicionesTemporada(99L, "lionel-messi");
        verify(estadisticas).upsert(any(EstadisticaPartido.class));
    }

    /**
     * Cuando obtenerApariciones devuelve vacío y el jugador no tiene slug guardado,
     * el fallback debe construir el slug a partir del nombre del jugador normalizado.
     */
    @Test
    void usaFallbackConSlugGeneradoDesdeNombreCuandoJugadorNoTieneSlug() {
        Jugador jugador = Jugador.builder()
                .id(8L)
                .idWhoscored(77L)
                .nombre("Vinicius Junior")
                .slug(null)
                .activo(true)
                .build();
        when(jugadores.obtenerJugadorPorId(8L)).thenReturn(jugador);
        when(scraping.obtenerApariciones(77L)).thenReturn(List.of());
        when(scraping.obtenerAparicionesTemporada(77L, "vinicius-junior")).thenReturn(List.of(
                new AparicionWhoscoredDto(30L, LocalDate.of(2025, 9, 15), "LaLiga", 2L,
                        10L, "Real Madrid", 11L, "Barcelona", "LOCAL", 90, new BigDecimal("8.2"),
                        Map.of())));
        when(estadisticas.buscarPorJugadorYPartido(8L, 30L)).thenReturn(null);

        ResultadoIngesta resultado = servicio.ingestarEstadisticasJugador(8L);

        assertThat(resultado.partidosInsertados()).isEqualTo(1);
        verify(scraping).obtenerAparicionesTemporada(77L, "vinicius-junior");
        verify(estadisticas).upsert(any(EstadisticaPartido.class));
    }

    /**
     * Cuando ambos métodos devuelven vacío, el contador de anomalías debe incrementarse
     * y no se deben persistir estadísticas.
     */
    @Test
    void registraAnomaliaYNoInsertaNadaCuandoAmbosMetodosDevuelvenVacio() {
        Jugador jugador = Jugador.builder()
                .id(9L)
                .idWhoscored(55L)
                .nombre("Jugador Sin Datos")
                .slug(null)
                .activo(true)
                .build();
        when(jugadores.obtenerJugadorPorId(9L)).thenReturn(jugador);
        when(scraping.obtenerApariciones(55L)).thenReturn(List.of());
        when(scraping.obtenerAparicionesTemporada(eq(55L), anyString())).thenReturn(List.of());

        ResultadoIngesta resultado = servicio.ingestarEstadisticasJugador(9L);

        assertThat(resultado.partidosInsertados()).isEqualTo(0);
        assertThat(resultado.partidosActualizados()).isEqualTo(0);
        verify(estadisticas, never()).upsert(any());
    }

    /**
     * Verifica que las apariciones ya existentes en BD incrementen partidos actualizados,
     * no insertados.
     */
    @Test
    void incrementaPartidosActualizadosSiLaAparicionYaExiste() {
        Jugador jugador = Jugador.builder().id(7L).idWhoscored(99L).activo(true).build();
        when(jugadores.obtenerJugadorPorId(7L)).thenReturn(jugador);

        AparicionWhoscoredDto aparicion = new AparicionWhoscoredDto(10L, LocalDate.of(2025, 9, 1), "Liga", 1L,
                2L, "Equipo", 3L, "Rival", "LOCAL", 90, BigDecimal.TEN, Map.of());
        when(scraping.obtenerApariciones(99L)).thenReturn(List.of(aparicion));

        EstadisticaPartido existente = EstadisticaPartido.builder()
                .jugador(jugador).partidoWhoscoredId(10L)
                .fechaPartido(LocalDate.of(2025, 9, 1)).minutos(90).build();
        when(estadisticas.buscarPorJugadorYPartido(7L, 10L)).thenReturn(existente);

        ResultadoIngesta resultado = servicio.ingestarEstadisticasJugador(7L);

        assertThat(resultado.partidosActualizados()).isEqualTo(1);
        assertThat(resultado.partidosInsertados()).isEqualTo(0);
        verify(estadisticas).upsert(any(EstadisticaPartido.class));
    }
}
