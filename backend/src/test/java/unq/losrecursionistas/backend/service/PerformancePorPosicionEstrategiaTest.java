package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.service.impl.estrategia.PerformancePorPosicionEstrategia;
import unq.losrecursionistas.backend.service.interfaces.*;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PerformancePorPosicionEstrategiaTest {
    @Test
    void reconocePosicionEspanolaEInglesa() {
        var estrategia = new PerformancePorPosicionEstrategia();
        var resumen = new ResumenRendimiento(10, 0, 10, 0, 0, 0, 0, 0, BigDecimal.ZERO);
        var maximos = Map.of("goles", 10, "tiros", 10, "rating", 0);
        var parametros = new ParametrosCotizacion(BigDecimal.ZERO, BigDecimal.valueOf(100), Map.of());
        assertThat(estrategia.calcular(resumen, "DELANTERO", maximos, parametros))
                .isEqualByComparingTo("55.00");
        assertThat(estrategia.calcular(resumen, "Forward", maximos, parametros))
                .isEqualByComparingTo("55.00");
    }

    @Test
    void posicionDesconocidaUsaFallbackSinFallar() {
        var estrategia = new PerformancePorPosicionEstrategia();
        var resumen = new ResumenRendimiento(1, 0, 0, 0, 0, 0, 0, 0, BigDecimal.ZERO);
        var maximos = Map.of("goles", 1, "rating", 0);
        assertThat(estrategia.calcular(resumen, "Otro", maximos,
                new ParametrosCotizacion(BigDecimal.TEN, BigDecimal.ONE, Map.of())))
                .isEqualByComparingTo("10.25");
    }
}
