package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.adapter.whoscored.ScrapingConfiguration;
import unq.losrecursionistas.backend.model.EjecucionIngesta;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEjecucionIngesta;
import unq.losrecursionistas.backend.service.impl.EjecucionIngestaServiceImpl;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

class EjecucionIngestaServiceImplTest {

    @Test
    void creaEjecucionConConfiguracionYAlcance() {
        RepositorioEjecucionIngesta repositorio = mock(RepositorioEjecucionIngesta.class);
        ScrapingConfiguration configuration = new ScrapingConfiguration();
        configuration.setTemporadaInicio(LocalDate.of(2025, 1, 1));
        configuration.setTemporadaFin(LocalDate.of(2025, 12, 31));
        EjecucionIngesta reservada = EjecucionIngesta.builder().id(1L).build();
        when(repositorio.reservar(any())).thenReturn(reservada);

        EjecucionIngesta result = new EjecucionIngestaServiceImpl(repositorio, configuration)
                .crearEjecucion(EjecucionIngesta.Alcance.LIGA, 5L);

        assertThat(result).isSameAs(reservada);
        var captor = forClass(EjecucionIngesta.class);
        verify(repositorio).reservar(captor.capture());
        assertThat(captor.getValue().getEstado()).isEqualTo(EjecucionIngesta.Estado.ACTIVA);
        assertThat(captor.getValue().getIdAlcance()).isEqualTo(5L);
    }

    @Test
    void obtieneUltimaEjecucion() {
        RepositorioEjecucionIngesta repositorio = mock(RepositorioEjecucionIngesta.class);
        EjecucionIngesta ultima = EjecucionIngesta.builder().id(2L).build();
        when(repositorio.ultima()).thenReturn(ultima);

        assertThat(new EjecucionIngestaServiceImpl(repositorio, new ScrapingConfiguration()).ultima())
                .isSameAs(ultima);
    }
}
