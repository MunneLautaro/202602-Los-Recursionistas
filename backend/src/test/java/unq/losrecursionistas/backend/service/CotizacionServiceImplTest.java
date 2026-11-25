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
import unq.losrecursionistas.backend.exceptions.ExcepcionRecursoNoEncontrado;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
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
    void testRecalculoExitosoRegistraCotizadasYVariacionPromedioSinContarJugadoresSinPrevio() {
        MetricasNegocio metricas = mock(MetricasNegocio.class);
        var j1 = jugadorConPosicion(1L, "Delantero");
        var j2 = jugadorConPosicion(2L, "Defensor");
        var j3 = jugadorConPosicion(3L, "Mediocampista");
        prepararEstadisticas(List.of(j1, j2, j3));
        when(estrategia.calcular(any(), eq("Delantero"), anyMap(), any())).thenReturn(new BigDecimal("110.00"));
        when(estrategia.calcular(any(), eq("Defensor"), anyMap(), any())).thenReturn(new BigDecimal("130.00"));
        when(estrategia.calcular(any(), eq("Mediocampista"), anyMap(), any())).thenReturn(new BigDecimal("50.00"));
        when(cotizaciones.vigente(1L)).thenReturn(cotizacionPrevia(j1, "100.00"));
        when(cotizaciones.vigente(2L)).thenReturn(cotizacionPrevia(j2, "100.00"));
        when(cotizaciones.vigente(3L)).thenReturn(null);

        servicioConMetricas(metricas, "PERFORMANCE_GENERAL").recalcular();

        var resultado = capturarRecalculo(metricas);
        assertTrue(resultado.exitoso());
        assertEquals(3, resultado.jugadoresCotizados());
        assertEquals(0, new BigDecimal("0.20").compareTo(resultado.variacionPromedio()));
        assertNotNull(resultado.duracion());
    }

    @Test
    void testEstrategiaInexistenteRegistraRecalculoFallidoYRelanza() {
        MetricasNegocio metricas = mock(MetricasNegocio.class);
        var service = servicioConMetricas(metricas, "INEXISTENTE");

        assertThrows(ExcepcionValidacion.class, () -> service.recalcular(List.of(), LocalDate.now(), LocalDate.now()));

        var resultado = capturarRecalculo(metricas);
        assertFalse(resultado.exitoso());
        assertEquals(0, resultado.jugadoresCotizados());
        assertNull(resultado.variacionPromedio());
    }

    @Test
    void testJugadorConCalculoFallidoNoCuentaComoCotizado() {
        MetricasNegocio metricas = mock(MetricasNegocio.class);
        var j1 = jugadorConPosicion(1L, "Delantero");
        var j2 = jugadorConPosicion(2L, "Arquero");
        prepararEstadisticas(List.of(j1, j2));
        when(estrategia.calcular(any(), eq("Delantero"), anyMap(), any())).thenReturn(new BigDecimal("90.00"));
        when(estrategia.calcular(any(), eq("Arquero"), anyMap(), any())).thenThrow(new IllegalStateException("sin datos"));

        servicioConMetricas(metricas, "PERFORMANCE_GENERAL").recalcular();

        var resultado = capturarRecalculo(metricas);
        assertTrue(resultado.exitoso());
        assertEquals(1, resultado.jugadoresCotizados());
        assertNull(resultado.variacionPromedio());
    }

    private CotizacionServiceImpl servicioConMetricas(MetricasNegocio metricas, String estrategiaActiva) {
        return new CotizacionServiceImpl(jugadores, estadisticas, cotizaciones,
                Map.of("PERFORMANCE_GENERAL", estrategia), null, estrategiaActiva, 7,
                BigDecimal.TEN, BigDecimal.ONE, metricas);
    }

    private static Jugador jugadorConPosicion(long id, String posicion) {
        return Jugador.builder().id(id).idWhoscored(100 + id).activo(true).posicion(posicion).build();
    }

    private void prepararEstadisticas(List<Jugador> lista) {
        when(jugadores.obtenerJugadoresActivos()).thenReturn(lista);
        for (Jugador jugador : lista) {
            var stat = EstadisticaPartido.builder().jugador(jugador).fechaPartido(LocalDate.of(2026, 10, 20))
                    .goles(1).minutos(90).build();
            when(estadisticas.buscarPorJugadorYRango(eq(jugador.getId()), any(), any())).thenReturn(List.of(stat));
        }
    }

    private static Cotizacion cotizacionPrevia(Jugador jugador, String valor) {
        return Cotizacion.crear(jugador, new BigDecimal(valor), LocalDateTime.of(2026, 11, 1, 10, 0),
                "PERFORMANCE_GENERAL", LocalDate.of(2026, 10, 25), LocalDate.of(2026, 11, 1));
    }

    private static MetricasNegocio.ResultadoRecalculo capturarRecalculo(MetricasNegocio metricas) {
        ArgumentCaptor<MetricasNegocio.ResultadoRecalculo> captor =
                ArgumentCaptor.forClass(MetricasNegocio.ResultadoRecalculo.class);
        verify(metricas).registrarRecalculo(captor.capture());
        return captor.getValue();
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
    void serieDevuelvePuntosEnOrdenCronologicoConTopeDePuntos() {
        var jugador = Jugador.builder().id(1L).activo(true).build();
        var desde = LocalDate.of(2026, 10, 1);
        var hasta = LocalDate.of(2026, 10, 31);
        var primera = Cotizacion.crear(jugador, new BigDecimal("100"),
                LocalDateTime.of(2026, 10, 5, 3, 0), "PERFORMANCE_GENERAL", desde, desde.plusDays(4));
        var segunda = Cotizacion.crear(jugador, new BigDecimal("120.555"),
                LocalDateTime.of(2026, 10, 12, 3, 0), "PERFORMANCE_GENERAL", desde, desde.plusDays(11));
        when(jugadores.existe(1L)).thenReturn(true);
        when(cotizaciones.buscarSeriePorJugador(1L, desde, hasta, CotizacionServiceImpl.MAX_PUNTOS_SERIE))
                .thenReturn(List.of(primera, segunda));

        var serie = nuevoServicio().serie(1L, desde, hasta);

        assertEquals(2, serie.size());
        assertEquals(LocalDateTime.of(2026, 10, 5, 3, 0), serie.get(0).getTimestampCalculo());
        assertEquals(new BigDecimal("100.00"), serie.get(0).getValor());
        assertEquals(new BigDecimal("120.56"), serie.get(1).getValor());
        assertEquals("PERFORMANCE_GENERAL", serie.get(1).getEstrategiaUsada());
    }

    @Test
    void serieRechazaRangoInvertidoSinConsultarRepositorio() {
        var servicio = nuevoServicio();

        assertThrows(ExcepcionValidacion.class, () -> servicio.serie(1L,
                LocalDate.of(2026, 10, 31), LocalDate.of(2026, 10, 1)));
        verifyNoInteractions(jugadores, cotizaciones);
    }

    @Test
    void serieDeJugadorInexistenteLanzaRecursoNoEncontrado() {
        when(jugadores.existe(99L)).thenReturn(false);
        var servicio = nuevoServicio();

        assertThrows(ExcepcionRecursoNoEncontrado.class, () -> servicio.serie(99L, null, null));
        verifyNoInteractions(cotizaciones);
    }

    @Test
    void serieSinCotizacionesDevuelveListaVacia() {
        when(jugadores.existe(1L)).thenReturn(true);
        when(cotizaciones.buscarSeriePorJugador(eq(1L), isNull(), isNull(), anyInt())).thenReturn(List.of());

        assertTrue(nuevoServicio().serie(1L, null, null).isEmpty());
    }

    private CotizacionServiceImpl nuevoServicio() {
        return new CotizacionServiceImpl(jugadores, estadisticas, cotizaciones,
                Map.of("PERFORMANCE_GENERAL", estrategia), "PERFORMANCE_GENERAL", 7,
                BigDecimal.TEN, BigDecimal.ONE);
    }
}
