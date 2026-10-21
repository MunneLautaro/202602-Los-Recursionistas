package unq.losrecursionistas.backend.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.service.interfaces.IngestaWhoscoredService;

@Component
@ConditionalOnProperty(name = "scheduler.whoscored.enabled", havingValue = "true", matchIfMissing = true)
public class WhoScoredScheduler {
    private final IngestaWhoscoredService servicio;

    public WhoScoredScheduler(IngestaWhoscoredService servicio) {
        this.servicio = servicio;
    }

    @Scheduled(cron = "${WHOSCORED_LINKING_CRON:0 0 2 * * SUN}",
            zone = "${scheduler.whoscored.zone:America/Argentina/Buenos_Aires}")
    public void ejecutarLinking() {
        servicio.ejecutarLinking();
    }

    @Scheduled(cron = "${WHOSCORED_STATS_CRON:0 0 3 * * SUN}",
            zone = "${scheduler.whoscored.zone:America/Argentina/Buenos_Aires}")
    public void ejecutarIngesta() {
        servicio.ejecutarIngesta();
    }
}
