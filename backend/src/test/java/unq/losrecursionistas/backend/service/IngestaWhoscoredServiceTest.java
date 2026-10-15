package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unq.losrecursionistas.backend.adapter.whoscored.ScrapingConfiguration;
import unq.losrecursionistas.backend.adapter.whoscored.ScrapingPort;
import unq.losrecursionistas.backend.adapter.whoscored.dto.AparicionWhoscoredDto;
import unq.losrecursionistas.backend.model.EjecucionIngesta;
import unq.losrecursionistas.backend.model.EstadisticaPartido;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioCasoVinculacion;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEjecucionIngesta;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEstadisticaPartido;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioJugador;
import unq.losrecursionistas.backend.service.impl.IngestaWhoscoredServiceImpl;
import unq.losrecursionistas.backend.service.impl.MatchingWhoscoredService;

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

    private ScrapingConfiguration configuracion;
    private IngestaWhoscoredServiceImpl servicio;

    @BeforeEach
    void setUp() {
        configuracion = new ScrapingConfiguration();
        configuracion.setTemporadaInicio(LocalDate.of(2025, 8, 1));
        configuracion.setTemporadaFin(LocalDate.of(2026, 6, 30));
        servicio = new IngestaWhoscoredServiceImpl(ejecuciones, jugadores, casos, estadisticas,
                matching, scraping, configuracion);
        when(ejecuciones.existeActiva()).thenReturn(false);
        when(ejecuciones.guardar(any())).thenAnswer(invocation -> invocation.getArgument(0));
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
}
