package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.service.impl.estrategia.PerformanceGeneralEstrategia;
import unq.losrecursionistas.backend.service.interfaces.*;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PerformanceGeneralEstrategiaTest {
    @Test
    void aplicaPesosYNormalizaMetricas() {
        var estrategia = new PerformanceGeneralEstrategia();
        var resumen = new ResumenRendimiento(10, 10, 10, 10, 10, 10, 0, 0, BigDecimal.TEN);
        var maximos = Map.of("goles", 10, "asistencias", 10, "tiros", 10,
                "pasesClave", 10, "regates", 10, "tackles", 10, "rating", 10);

        assertThat(estrategia.calcular(resumen, "Delantero", maximos,
                new ParametrosCotizacion(BigDecimal.TEN, new BigDecimal("100"), Map.of())))
                .isEqualByComparingTo("110.00");
    }

    @Test
    void maximoCeroNoDividePorCero() {
        var estrategia = new PerformanceGeneralEstrategia();
        var resumen = new ResumenRendimiento(2, 0, 0, 0, 0, 0, 0, 0, BigDecimal.ZERO);
        var maximos = Map.of("goles", 0, "asistencias", 0, "tiros", 0,
                "pasesClave", 0, "regates", 0, "tackles", 0, "rating", 0);
        assertThat(estrategia.calcular(resumen, "", maximos,
                new ParametrosCotizacion(BigDecimal.TEN, BigDecimal.ONE, Map.of())))
                .isEqualByComparingTo("10.00");
    }
}
