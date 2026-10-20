package unq.losrecursionistas.backend.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.model.EjecucionIngesta;
import unq.losrecursionistas.backend.service.interfaces.IngestaWhoscoredService;

@Component
@ConditionalOnProperty(name = "scheduler.whoscored.enabled", havingValue = "true", matchIfMissing = true)
public class WhoScoredScheduler {
    private final IngestaWhoscoredService servicio;

    public WhoScoredScheduler(IngestaWhoscoredService servicio) {
        this.servicio = servicio;
    }

    @Scheduled(cron = "${scheduler.whoscored.cron}", zone = "${scheduler.whoscored.zone:America/Argentina/Buenos_Aires}")
    public void ejecutar() {
        servicio.iniciar(EjecucionIngesta.Alcance.COMPLETA, null);
    }
}
