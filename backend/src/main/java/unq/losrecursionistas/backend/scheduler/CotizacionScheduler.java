package unq.losrecursionistas.backend.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.service.interfaces.CotizacionService;

@Component
@ConditionalOnProperty(name = "scheduler.cotizacion.enabled", havingValue = "true", matchIfMissing = true)
public class CotizacionScheduler {
    private final CotizacionService servicio;

    public CotizacionScheduler(CotizacionService servicio) {
        this.servicio = servicio;
    }

    @Scheduled(cron = "${scheduler.cotizacion.cron}", zone = "${scheduler.cotizacion.zone:America/Argentina/Buenos_Aires}")
    public void ejecutarCotizacion() {
        servicio.recalcular();
    }
}
