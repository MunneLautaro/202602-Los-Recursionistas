package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.exceptions.ExcepcionDominio;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;
import unq.losrecursionistas.backend.model.EjecucionIngesta;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.model.Liga;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioLiga;
import unq.losrecursionistas.backend.service.impl.LigaServiceImpl;
import unq.losrecursionistas.backend.service.interfaces.ParametrosCotizacion;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RemainingSmallCoverageTest {

    @Test
    void validaParametrosYExcepciones() {
        BigDecimal one = BigDecimal.ONE;
        Map<String, BigDecimal> emptyWeights = Map.of();
        ParametrosCotizacion parameters = new ParametrosCotizacion(BigDecimal.TEN, BigDecimal.ONE,
                Map.of("goles", BigDecimal.ONE));
        assertThat(parameters.pesos()).containsKey("goles");
        assertThatThrownBy(() -> new ParametrosCotizacion(null, one, emptyWeights))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ParametrosCotizacion(one, null, emptyWeights))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ParametrosCotizacion(one, one, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(new ExcepcionDominio("x", new IllegalStateException()).getCause()).isInstanceOf(IllegalStateException.class);
        assertThat(new ExcepcionValidacion("x", new IllegalStateException()).getCause()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ejecutaServicioDeLigas() {
        RepositorioLiga repository = mock(RepositorioLiga.class);
        when(repository.obtenerLigas()).thenReturn(List.of(new Liga(1L, "Liga", "L")));

        assertThat(new LigaServiceImpl(repository).obtenerLigas()).hasSize(1);
    }

    @Test
    void configuraAlcancesDeEjecucion() {
        EjecucionIngesta execution = EjecucionIngesta.builder()
                .alcance(EjecucionIngesta.Alcance.LIGA).build();
        execution.setIdAlcance(1L);
        assertThat(execution.getIdAlcance()).isEqualTo(1L);
        execution.setAlcance(EjecucionIngesta.Alcance.EQUIPO);
        execution.setIdAlcance(2L);
        assertThat(execution.getIdAlcance()).isEqualTo(2L);
        execution.setAlcance(EjecucionIngesta.Alcance.JUGADOR);
        execution.setIdAlcance(3L);
        assertThat(execution.getIdAlcance()).isEqualTo(3L);
        execution.setAlcance(EjecucionIngesta.Alcance.COMPLETA);
        execution.setIdAlcance(4L);
        assertThat(execution.getIdAlcance()).isNull();
        execution.setAlcance(null);
        execution.setIdAlcance(5L);
        assertThatThrownBy(execution::getIdAlcance).isInstanceOf(NullPointerException.class);
    }
}
