package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.service.impl.CotizacionAsyncWorker;
import unq.losrecursionistas.backend.service.interfaces.CotizacionService;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

class CotizacionAsyncWorkerTest {

    @Test
    void recalculaYLiberarCuandoFinaliza() {
        CotizacionService service = mock(CotizacionService.class);
        CotizacionAsyncWorker worker = new CotizacionAsyncWorker(service);
        LocalDate inicio = LocalDate.of(2026, 1, 1);
        LocalDate fin = LocalDate.of(2026, 1, 7);

        worker.ejecutar(inicio, fin);

        verify(service).recalcular(inicio, fin);
        verify(service).liberarRecalculo();
    }

    @Test
    void liberaAunqueElRecalculoFalle() {
        CotizacionService service = mock(CotizacionService.class);
        doThrow(new IllegalStateException("fallo")).when(service)
                .recalcular(any(LocalDate.class), any(LocalDate.class));
        CotizacionAsyncWorker worker = new CotizacionAsyncWorker(service);

        worker.ejecutar(LocalDate.now(), LocalDate.now());

        verify(service).liberarRecalculo();
    }
}
