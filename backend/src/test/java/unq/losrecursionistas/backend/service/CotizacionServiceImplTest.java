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
}
