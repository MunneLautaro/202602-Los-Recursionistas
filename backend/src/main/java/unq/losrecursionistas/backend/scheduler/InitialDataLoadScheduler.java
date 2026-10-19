package unq.losrecursionistas.backend.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.adapter.whoscored.ScrapingConfiguration;
import unq.losrecursionistas.backend.service.impl.SincronizacionService;
import unq.losrecursionistas.backend.service.impl.WhoscoredBootstrapService;

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

    public InitialDataLoadScheduler(SincronizacionService footballData,
                                    WhoscoredBootstrapService whoscored,
                                    ScrapingConfiguration scrapingConfiguration,
                                    @Value("${football.api.key}") String footballDataApiKey,
                                    @Value("${scheduler.football-data.startup-enabled:true}") boolean footballDataStartupEnabled) {
        this.footballData = footballData;
        this.whoscored = whoscored;
        this.scrapingConfiguration = scrapingConfiguration;
        this.footballDataApiKey = footballDataApiKey;
        this.footballDataStartupEnabled = footballDataStartupEnabled;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void cargarDatosIniciales() {
        LOGGER.info("Iniciando carga inicial de Football Data y WhoScored...");
        if (!footballDataStartupEnabled) {
            LOGGER.info("Carga inicial de Football Data deshabilitada por FOOTBALL_DATA_STARTUP_ENABLED=false.");
        } else if (!esTokenValido()) {
            LOGGER.error("Carga inicial de Football Data omitida: configure FOOTBALL_DATA_API_KEY en backend/.env con un token real.");
        } else {
            footballData.sincronizarTodasLasLigas(LIGAS);
        }
        if (!scrapingConfiguration.isStartupEnabled()) {
            LOGGER.info("Carga inicial de WhoScored deshabilitada por WHOSCORED_STARTUP_ENABLED=false.");
            return;
        }
        try {
            whoscored.cargarDatosIniciales();
            LOGGER.info("Carga inicial de WhoScored finalizada.");
        } catch (RuntimeException exception) {
            LOGGER.error("La carga inicial de WhoScored fallo. Verifique Playwright, conectividad y la configuracion de scraping.", exception);
        }
    }

    private boolean esTokenValido() {
        return footballDataApiKey != null
                && !footballDataApiKey.isBlank()
                && !"replace_with_your_football_data_api_key".equals(footballDataApiKey);
    }
}
