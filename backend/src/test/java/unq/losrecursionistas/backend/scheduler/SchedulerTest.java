package unq.losrecursionistas.backend.scheduler;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioJugador;
import unq.losrecursionistas.backend.service.impl.SincronizacionService;
import unq.losrecursionistas.backend.service.interfaces.CotizacionService;
import unq.losrecursionistas.backend.service.interfaces.LiquidezInicialService;

import java.util.List;

import static org.mockito.Mockito.*;

class SchedulerTest {

    @Test
    void procesaJugadoresYContinuaTrasUnError() {
        CotizacionService cotizaciones = mock(CotizacionService.class);
        RepositorioJugador jugadores = mock(RepositorioJugador.class);
        LiquidezInicialService liquidez = mock(LiquidezInicialService.class);
        Jugador primero = Jugador.builder().id(1L).nombre("Uno").build();
        Jugador segundo = Jugador.builder().id(2L).nombre("Dos").build();
        when(jugadores.obtenerJugadoresActivos()).thenReturn(List.of(primero, segundo));
        doThrow(new IllegalStateException("fallo")).when(liquidez).inicializar(primero);

        new CotizacionScheduler(cotizaciones, jugadores, liquidez).ejecutarCotizacion();

        verify(cotizaciones).recalcular();
        verify(liquidez).inicializar(primero);
        verify(liquidez).inicializar(segundo);
    }

    @Test
    void sincronizaFootballDataCuandoNoHayError() {
        SincronizacionService sincronizacion = mock(SincronizacionService.class);

        new FootballDataScheduler(sincronizacion).ejecutarSincronizacionSemanal();

        verify(sincronizacion).sincronizarTodasLasLigas(List.of("PL", "BL1", "PD", "SA", "FL1"));
    }

    @Test
    void capturaErrorDeSincronizacion() {
        SincronizacionService sincronizacion = mock(SincronizacionService.class);
        doThrow(new IllegalStateException("fallo")).when(sincronizacion)
                .sincronizarTodasLasLigas(anyList());

        new FootballDataScheduler(sincronizacion).ejecutarSincronizacionSemanal();

        verify(sincronizacion).sincronizarTodasLasLigas(anyList());
    }
}
