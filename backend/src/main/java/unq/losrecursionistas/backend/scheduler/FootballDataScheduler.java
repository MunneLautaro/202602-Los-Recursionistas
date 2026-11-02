package unq.losrecursionistas.backend.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.service.impl.SincronizacionService;

@Component
public class FootballDataScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FootballDataScheduler.class);

    private final SincronizacionService sincronizacionService;

    public FootballDataScheduler(SincronizacionService sincronizacionService) {
        this.sincronizacionService = sincronizacionService;
    }

    @Scheduled(cron = "${scheduler.football-data.cron}", zone = "America/Argentina/Buenos_Aires")
    public void ejecutarSincronizacionSemanal() {
        LOGGER.info("Ejecutando tarea programada semanal de sincronización de Football Data...");
        ejecutarSincronizacion();
    }

    private void ejecutarSincronizacion() {
        try {
            sincronizacionService.sincronizarTodasLasLigas(java.util.List.of("PL", "BL1", "PD", "SA", "FL1"));
            LOGGER.info("Sincronización semanal de Football Data completada exitosamente.");
        } catch (Exception e) {
            LOGGER.error("Error al ejecutar la sincronización periódica de Football Data: {}", e.getMessage(), e);
        }
    }
}
