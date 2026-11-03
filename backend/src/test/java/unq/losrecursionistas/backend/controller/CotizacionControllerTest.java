package unq.losrecursionistas.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import unq.losrecursionistas.backend.service.impl.CotizacionAsyncWorker;
import unq.losrecursionistas.backend.service.interfaces.CotizacionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CotizacionControllerTest {

    @Test
    void aceptaElRecalculoCuandoSePuedeReservar() {
        CotizacionService service = mock(CotizacionService.class);
        CotizacionAsyncWorker worker = mock(CotizacionAsyncWorker.class);
        when(service.reservarRecalculo()).thenReturn(true);
        CotizacionController controller = new CotizacionController(service, worker, 7);

        var response = controller.recalcular();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).containsEntry("mensaje", "Recálculo de cotizaciones aceptado");
        verify(worker).ejecutar(any(), any());
    }

    @Test
    void rechazaRecalculoSiYaHayUnoActivo() {
        CotizacionService service = mock(CotizacionService.class);
        when(service.reservarRecalculo()).thenReturn(false);
        CotizacionAsyncWorker worker = mock(CotizacionAsyncWorker.class);
        CotizacionController controller = new CotizacionController(service, worker, 7);

        assertThatThrownBy(controller::recalcular)
                .isInstanceOf(unq.losrecursionistas.backend.exceptions.ExcepcionCotizacionActiva.class);
    }

    @Test
    void liberaLaReservaSiElWorkerFalla() {
        CotizacionService service = mock(CotizacionService.class);
        CotizacionAsyncWorker worker = mock(CotizacionAsyncWorker.class);
        when(service.reservarRecalculo()).thenReturn(true);
        doThrow(new IllegalStateException("fallo")).when(worker).ejecutar(any(), any());
        CotizacionController controller = new CotizacionController(service, worker, 7);

        assertThatThrownBy(controller::recalcular).isInstanceOf(IllegalStateException.class);
        verify(service).liberarRecalculo();
    }

    @Test
    void delegaLasConsultasDeHistorialYRanking() {
        CotizacionService service = mock(CotizacionService.class);
        when(service.historial(anyLong(), isNull(), isNull(), eq(0), eq(12)))
                .thenReturn(Page.empty());
        when(service.ranking(isNull(), isNull(), eq(0), eq(12)))
                .thenReturn(Page.empty());
        CotizacionAsyncWorker worker = mock(CotizacionAsyncWorker.class);
        CotizacionController controller = new CotizacionController(service, worker, 7);

        assertThat(controller.historial(1L, null, null, 0, 12)).isEmpty();
        assertThat(controller.ranking(null, null, 0, 12)).isEmpty();
    }
}
