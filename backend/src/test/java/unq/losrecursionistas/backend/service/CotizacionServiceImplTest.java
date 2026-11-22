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
        assertEquals(LocalDateTime.of(2026, 10, 5, 3, 0), serie.get(0).fecha());
        assertEquals(new BigDecimal("100.00"), serie.get(0).valor());
        assertEquals(new BigDecimal("120.56"), serie.get(1).valor());
        assertEquals("PERFORMANCE_GENERAL", serie.get(1).estrategiaUsada());
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
