package unq.losrecursionistas.backend.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.adapter.whoscored.ScrapingConfiguration;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioJugador;
import unq.losrecursionistas.backend.service.impl.SincronizacionService;
import unq.losrecursionistas.backend.service.impl.WhoscoredBootstrapService;
import unq.losrecursionistas.backend.service.interfaces.CotizacionService;
import unq.losrecursionistas.backend.service.interfaces.LiquidezInicialService;

import java.util.List;

@Component
public class InitialDataLoadScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitialDataLoadScheduler.class);
    private static final List<String> LIGAS = List.of("PL", "BL1", "PD", "SA", "FL1");

    private final SincronizacionService footballData;
    private final WhoscoredBootstrapService whoscored;
    private final ScrapingConfiguration scrapingConfiguration;
    private final String footballDataApiKey;
    private final boolean footballDataStartupEnabled;
    private final RepositorioJugador jugadores;
    private final LiquidezInicialService liquidezInicial;
    private final CotizacionService cotizacionService;

    public InitialDataLoadScheduler(SincronizacionService footballData,
                                    WhoscoredBootstrapService whoscored,
                                    ScrapingConfiguration scrapingConfiguration,
                                    @Value("${football.api.key}") String footballDataApiKey,
                                    @Value("${scheduler.football-data.startup-enabled:true}") boolean footballDataStartupEnabled,
                                    RepositorioJugador jugadores,
                                    LiquidezInicialService liquidezInicial,
                                    CotizacionService cotizacionService) {
        this.footballData = footballData;
        this.whoscored = whoscored;
        this.scrapingConfiguration = scrapingConfiguration;
        this.footballDataApiKey = footballDataApiKey;
        this.footballDataStartupEnabled = footballDataStartupEnabled;
        this.jugadores = jugadores;
        this.liquidezInicial = liquidezInicial;
        this.cotizacionService = cotizacionService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void cargarDatosIniciales() {
        LOGGER.info("========== Iniciando proceso de carga inicial y preparación del sistema ==========");
        sincronizarFootballData();
        cargarWhoScored();
        recalcularCotizaciones();
        inicializarLiquidez();
        LOGGER.info("========== Proceso de inicialización de la aplicación completado ==========");
    }

    private void sincronizarFootballData() {
        if (!footballDataStartupEnabled) {
            LOGGER.info("[Football Data] Carga inicial deshabilitada por configuración (FOOTBALL_DATA_STARTUP_ENABLED=false).");
        } else if (!esTokenValido()) {
            LOGGER.error("[Football Data] Carga inicial omitida: configure FOOTBALL_DATA_API_KEY en backend/.env con un token real.");
        } else {
            LOGGER.info("[Football Data] Iniciando sincronización de ligas: {}", LIGAS);
            try {
                footballData.sincronizarTodasLasLigas(LIGAS);
                LOGGER.info("[Football Data] Sincronización completada exitosamente.");
            } catch (Exception e) {
                LOGGER.error("[Football Data] Ocurrió un error durante la sincronización: {}", e.getMessage(), e);
            }
        }
    }

    private void cargarWhoScored() {
        if (!scrapingConfiguration.isStartupEnabled()) {
            LOGGER.info("[WhoScored] Carga inicial deshabilitada por configuración (WHOSCORED_STARTUP_ENABLED=false).");
        } else {
            LOGGER.info("[WhoScored] Iniciando descubrimiento, vinculación y scraping inicial...");
            try {
                whoscored.cargarDatosIniciales();
                LOGGER.info("[WhoScored] Carga inicial finalizada exitosamente.");
            } catch (RuntimeException exception) {
                LOGGER.error("[WhoScored] La carga inicial falló. Verifique Playwright, conectividad y scraping: {}",
                        exception.getMessage(), exception);
            }
        }
    }

    private void recalcularCotizaciones() {
        if (cotizacionService != null) {
            try {
                LOGGER.info("[Cotizaciones] Recalculando cotizaciones por rendimiento...");
                cotizacionService.recalcular();
                LOGGER.info("[Cotizaciones] Recálculo completado.");
            } catch (Exception e) {
                LOGGER.warn("[Cotizaciones] No se pudieron recalcular cotizaciones de rendimiento: {}", e.getMessage());
            }
        }
    }

    private void inicializarLiquidez() {
        LOGGER.info("[Mercado] Inicializando liquidez (emisión de tokens y órdenes de venta iniciales)...");
        if (jugadores == null || liquidezInicial == null) {
            return;
        }
        List<Jugador> jugadoresActivos = jugadores.obtenerJugadoresActivos();
        LOGGER.info("[Mercado] Se encontraron {} jugadores activos para verificar liquidez inicial.", jugadoresActivos.size());
        int procesados = 0;
        int errores = 0;
        for (Jugador jugador : jugadoresActivos) {
            if (inicializarLiquidez(jugador)) {
                procesados++;
            } else {
                errores++;
            }
        }
        LOGGER.info("[Mercado] Proceso de liquidez inicial finalizado. Jugadores procesados: {}, Errores: {}.",
                procesados, errores);
    }

    private boolean inicializarLiquidez(Jugador jugador) {
        try {
            liquidezInicial.inicializar(jugador);
            return true;
        } catch (RuntimeException exception) {
            LOGGER.warn("[Mercado] No se pudo inicializar liquidez para el jugador '{}' (ID: {}): {}",
                    jugador.getNombre(), jugador.getId(), exception.getMessage());
            return false;
        }
    }

    private boolean esTokenValido() {
        return footballDataApiKey != null
                && !footballDataApiKey.isBlank()
                && !"replace_with_your_football_data_api_key".equals(footballDataApiKey);
    }
}
