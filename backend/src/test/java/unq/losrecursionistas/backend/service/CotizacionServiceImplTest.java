package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import unq.losrecursionistas.backend.model.*;
import unq.losrecursionistas.backend.persistence.repository.interfaces.*;
import unq.losrecursionistas.backend.service.impl.CotizacionServiceImpl;
import unq.losrecursionistas.backend.service.interfaces.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageImpl;
import unq.losrecursionistas.backend.exceptions.ExcepcionRecursoNoEncontrado;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;
import unq.losrecursionistas.backend.persistence.sql.CotizacionDAOSQL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CotizacionServiceImplTest {
    @Mock RepositorioJugador jugadores;
    @Mock RepositorioEstadisticaPartido estadisticas;
    @Mock RepositorioCotizacion cotizaciones;
    @Mock EstrategiaCotizacion estrategia;

    @Test
    void procesaJugadorActivoConIdYEstadisticas() {
        var jugador = Jugador.builder().id(1L).idWhoscored(9L).activo(true).posicion("Delantero").build();
        var stat = EstadisticaPartido.builder().jugador(jugador).fechaPartido(LocalDate.of(2026, 10, 20))
                .goles(2).minutos(90).build();
        when(jugadores.obtenerJugadoresActivos()).thenReturn(List.of(jugador));
        when(estadisticas.buscarPorJugadorYRango(eq(1L), any(), any())).thenReturn(List.of(stat));
        when(estrategia.calcular(any(), any(), any(), any())).thenReturn(new BigDecimal("100.00"));
        var service = new CotizacionServiceImpl(jugadores, estadisticas, cotizaciones,
                Map.of("PERFORMANCE_GENERAL", estrategia), "PERFORMANCE_GENERAL", 7,
                BigDecimal.TEN, BigDecimal.ONE);

        service.recalcular();

        verify(cotizaciones).guardar(any(Cotizacion.class));
        verify(estrategia).calcular(any(), eq("Delantero"), anyMap(), any());
    }

    @Test
    void omiteJugadorSinIdWhoscored() {
        var jugador = Jugador.builder().id(1L).activo(true).build();
        when(jugadores.obtenerJugadoresActivos()).thenReturn(List.of(jugador));
        var service = new CotizacionServiceImpl(jugadores, estadisticas, cotizaciones,
                Map.of("PERFORMANCE_GENERAL", estrategia), "PERFORMANCE_GENERAL", 7,
                BigDecimal.TEN, BigDecimal.ONE);

        service.recalcular();

        verifyNoInteractions(estadisticas, cotizaciones, estrategia);
    }

    @Test
    void recalculaConRangoExplicitoYOmiteDatosVacios() {
        var jugador = Jugador.builder().id(1L).idWhoscored(9L).activo(true).build();
        when(jugadores.obtenerJugadoresActivos()).thenReturn(List.of(jugador));
        when(estadisticas.buscarPorJugadorYRango(eq(1L), any(), any())).thenReturn(List.of());
        var service = crearServicio(Map.of("PERFORMANCE_GENERAL", estrategia), "PERFORMANCE_GENERAL",
                BigDecimal.TEN, BigDecimal.ONE);

        service.recalcular(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 7));

        verify(estadisticas).buscarPorJugadorYRango(1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 7));
        verifyNoInteractions(cotizaciones, estrategia);
    }

    @Test
    void rechazaConfiguracionInvalidaDeRecalculo() {
        var service = crearServicio(Map.of(), "INEXISTENTE", BigDecimal.TEN, BigDecimal.ONE);
        var inicioInvalido = LocalDate.of(2026, 1, 2);
        var finInvalido = LocalDate.of(2026, 1, 1);
        List<Jugador> jugadoresVacios = List.of();

        assertThatThrownBy(() -> service.recalcular(jugadoresVacios, inicioInvalido, finInvalido))
                .isInstanceOf(ExcepcionValidacion.class);
        var servicioConEstrategia = crearServicio(Map.of("PERFORMANCE_GENERAL", estrategia),
                "PERFORMANCE_GENERAL", BigDecimal.TEN, BigDecimal.ONE);
        var fechaFin = LocalDate.of(2026, 1, 1);
        assertThatThrownBy(() -> servicioConEstrategia.recalcular(jugadoresVacios, null, fechaFin))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void continuaTrasErrorDeLaEstrategia() {
        var jugador = Jugador.builder().id(1L).idWhoscored(9L).activo(true).build();
        var stat = EstadisticaPartido.builder().jugador(jugador).goles(1).rating(BigDecimal.TEN).build();
        when(estadisticas.buscarPorJugadorYRango(eq(1L), any(), any())).thenReturn(List.of(stat));
        when(estrategia.calcular(any(), any(), any(), any())).thenThrow(new IllegalArgumentException("fallo"));
        var service = crearServicio(Map.of("performance_general", estrategia), "PERFORMANCE_GENERAL",
                BigDecimal.TEN, BigDecimal.ONE);

        service.recalcular(List.of(jugador), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 7));

        verify(cotizaciones, never()).guardar(any());
    }

    @Test
    void creaCotizacionInicialSoloSiNoExiste() {
        var jugador = Jugador.builder().id(1L).activo(true).nombre("Jugador").build();
        var service = crearServicio(Map.of(), "PERFORMANCE_GENERAL", BigDecimal.ZERO, BigDecimal.ONE);
        when(cotizaciones.vigente(1L)).thenReturn(null);

        service.asegurarCotizacionInicial(jugador);

        verify(cotizaciones).guardar(any(Cotizacion.class));
        service.asegurarCotizacionInicial(null);
        service.asegurarCotizacionInicial(Jugador.builder().id(2L).activo(false).build());
        verify(cotizaciones, never()).vigente(2L);
    }

    @Test
    void noCreaCotizacionInicialSiYaExiste() {
        var jugador = Jugador.builder().id(1L).activo(true).build();
        when(cotizaciones.vigente(1L)).thenReturn(Cotizacion.crear(jugador, BigDecimal.TEN,
                LocalDate.now().atStartOfDay(), "TEST", LocalDate.now(), LocalDate.now()));
        var service = crearServicio(Map.of(), "PERFORMANCE_GENERAL", BigDecimal.TEN, BigDecimal.ONE);

        service.asegurarCotizacionInicial(jugador);

        verify(cotizaciones, never()).guardar(any());
    }

    @Test
    void consultaHistorialYValidaSusParametros() {
        var jugador = Jugador.builder().id(1L).nombre("Jugador").build();
        when(jugadores.existe(1L)).thenReturn(true);
        when(cotizaciones.buscarPorJugador(eq(1L), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(Cotizacion.crear(jugador, BigDecimal.TEN,
                        LocalDate.now().atStartOfDay(), "TEST", LocalDate.now(), LocalDate.now()))));
        var service = crearServicio(Map.of(), "PERFORMANCE_GENERAL", BigDecimal.TEN, BigDecimal.ONE);

        assertThat(service.historial(1L, null, null, 0, 10)).hasSize(1);
        var fechaInicioInvalida = LocalDate.of(2026, 2, 1);
        var fechaFinInvalida = LocalDate.of(2026, 1, 1);
        assertThatThrownBy(() -> service.historial(1L, fechaInicioInvalida,
                fechaFinInvalida, 0, 10)).isInstanceOf(ExcepcionValidacion.class);
        assertThatThrownBy(() -> service.historial(1L, null, null, -1, 10))
                .isInstanceOf(ExcepcionValidacion.class);
        when(jugadores.existe(99L)).thenReturn(false);
        assertThatThrownBy(() -> service.historial(99L, null, null, 0, 10))
                .isInstanceOf(ExcepcionRecursoNoEncontrado.class);
    }

    @Test
    void transformaRankingYNormalizaLaPosicion() {
        var proyeccion = mock(CotizacionDAOSQL.RankingProyeccion.class);
        when(proyeccion.getJugadorId()).thenReturn(1L);
        when(proyeccion.getNombre()).thenReturn("Jugador");
        when(proyeccion.getPosicion()).thenReturn("Delantero");
        when(cotizaciones.buscarRanking(eq(2L), eq("delantero"), any()))
                .thenReturn(new PageImpl<>(List.of(proyeccion)));
        var service = crearServicio(Map.of(), "PERFORMANCE_GENERAL", BigDecimal.TEN, BigDecimal.ONE);

        assertThat(service.ranking(2L, " Delantéro ", 0, 10)).hasSize(1);
        verify(cotizaciones).buscarRanking(eq(2L), eq("delantero"), any());
    }

    @Test
    void controlaReservaDeRecalculo() {
        var service = crearServicio(Map.of(), "PERFORMANCE_GENERAL", BigDecimal.TEN, BigDecimal.ONE);

        assertThat(service.reservarRecalculo()).isTrue();
        assertThat(service.reservarRecalculo()).isFalse();
        service.liberarRecalculo();
        assertThat(service.reservarRecalculo()).isTrue();
    }

    private CotizacionServiceImpl crearServicio(Map<String, EstrategiaCotizacion> estrategias,
                                                 String estrategiaActiva, BigDecimal valorBase,
                                                 BigDecimal factorEscala) {
        return new CotizacionServiceImpl(jugadores, estadisticas, cotizaciones, estrategias,
                estrategiaActiva, 7, valorBase, factorEscala);
    }
}
