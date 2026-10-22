package unq.losrecursionistas.backend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.service.interfaces.CotizacionService;

import java.time.LocalDate;

@Component
public class CotizacionAsyncWorker {
    private static final Logger log = LoggerFactory.getLogger(CotizacionAsyncWorker.class);
    private final CotizacionService servicio;

    public CotizacionAsyncWorker(CotizacionService servicio) {
        this.servicio = servicio;
    }

    @Async("cotizacionTaskExecutor")
    public void ejecutar(LocalDate inicio, LocalDate fin) {
        try {
            servicio.recalcular(inicio, fin);
        } catch (RuntimeException ex) {
            log.error("Fallo el recálculo asincrónico de cotizaciones", ex);
        } finally {
            servicio.liberarRecalculo();
        }
    }

}
