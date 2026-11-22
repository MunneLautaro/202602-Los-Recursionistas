package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.service.impl.estrategia.PerformanceGeneralEstrategia;
import unq.losrecursionistas.backend.service.impl.estrategia.PerformancePorPosicionEstrategia;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El nombre del bean de cada estrategia debe coincidir con el valor admitido por
 * {@code cotizacion.estrategia.activa}; si no, el recálculo falla en runtime.
 */
class EstrategiaCotizacionNombreBeanTest {
    @Test
    void performanceGeneralSeRegistraConElNombreConfigurable() {
        assertThat(PerformanceGeneralEstrategia.class.getAnnotation(Component.class).value())
                .isEqualTo("PERFORMANCE_GENERAL");
    }

    @Test
    void performancePorPosicionSeRegistraConElNombreConfigurable() {
        assertThat(PerformancePorPosicionEstrategia.class.getAnnotation(Component.class).value())
                .isEqualTo("PERFORMANCE_POR_POSICION");
    }
}
