package unq.losrecursionistas.backend.scheduler;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.service.interfaces.IngestaWhoscoredService;

import static org.mockito.Mockito.*;

class WhoScoredSchedulerTest {

    @Test
    void ejecutaTareasProgramadas() {
        IngestaWhoscoredService service = mock(IngestaWhoscoredService.class);
        WhoScoredScheduler scheduler = new WhoScoredScheduler(service);

        scheduler.ejecutarLinking();
        scheduler.ejecutarIngesta();

        verify(service).ejecutarLinking();
        verify(service).ejecutarIngesta();
    }
}
