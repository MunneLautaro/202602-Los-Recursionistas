package unq.losrecursionistas.backend.scheduler;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.adapter.whoscored.ScrapingConfiguration;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioJugador;
import unq.losrecursionistas.backend.service.impl.SincronizacionService;
import unq.losrecursionistas.backend.service.impl.WhoscoredBootstrapService;
import unq.losrecursionistas.backend.service.interfaces.CotizacionService;
import unq.losrecursionistas.backend.service.interfaces.LiquidezInicialService;

import java.util.List;

import static org.mockito.Mockito.*;

class InitialDataLoadSchedulerTest {

    @Test
    void ejecutaCargaCompletaConConfiguracionValida() {
        SincronizacionService football = mock(SincronizacionService.class);
        WhoscoredBootstrapService whoscored = mock(WhoscoredBootstrapService.class);
        RepositorioJugador jugadores = mock(RepositorioJugador.class);
        LiquidezInicialService liquidez = mock(LiquidezInicialService.class);
        CotizacionService cotizaciones = mock(CotizacionService.class);
        ScrapingConfiguration config = new ScrapingConfiguration();
        Jugador jugador = Jugador.builder().id(1L).nombre("Jugador").build();
        when(jugadores.obtenerJugadoresActivos()).thenReturn(List.of(jugador));
        InitialDataLoadScheduler scheduler = new InitialDataLoadScheduler(football, whoscored, config,
                "real-key", true, jugadores, liquidez, cotizaciones);

        scheduler.cargarDatosIniciales();

        verify(football).sincronizarTodasLasLigas(anyList());
        verify(whoscored).cargarDatosIniciales();
        verify(cotizaciones).recalcular();
        verify(liquidez).inicializar(jugador);
    }

    @Test
    void saltaIntegracionesDeshabilitadasYContinuaTrasErrores() {
        ScrapingConfiguration config = new ScrapingConfiguration();
        config.setStartupEnabled(false);
        SincronizacionService football = mock(SincronizacionService.class);
        WhoscoredBootstrapService whoscored = mock(WhoscoredBootstrapService.class);
        CotizacionService cotizaciones = mock(CotizacionService.class);
        RepositorioJugador jugadores = mock(RepositorioJugador.class);
        LiquidezInicialService liquidez = mock(LiquidezInicialService.class);
        when(jugadores.obtenerJugadoresActivos()).thenReturn(List.of(
                Jugador.builder().id(1L).nombre("Uno").build()));
        doThrow(new RuntimeException("fallo")).when(cotizaciones).recalcular();
        doThrow(new RuntimeException("fallo")).when(liquidez).inicializar(any());
        InitialDataLoadScheduler scheduler = new InitialDataLoadScheduler(football, whoscored, config,
                "replace_with_your_football_data_api_key", false, jugadores, liquidez, cotizaciones);

        scheduler.cargarDatosIniciales();

        verifyNoInteractions(football, whoscored);
        verify(cotizaciones).recalcular();
        verify(liquidez).inicializar(any());
    }
}
