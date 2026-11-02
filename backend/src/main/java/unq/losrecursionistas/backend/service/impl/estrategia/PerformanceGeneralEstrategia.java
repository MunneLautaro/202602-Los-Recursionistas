package unq.losrecursionistas.backend.service.impl.estrategia;

import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.service.interfaces.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Component("PERFORMANCE_GENERAL")
public class PerformanceGeneralEstrategia implements EstrategiaCotizacion {
    private static final Map<String, BigDecimal> PESOS = Map.of(
            "goles", new BigDecimal("0.25"), "asistencias", new BigDecimal("0.15"),
            "tiros", new BigDecimal("0.10"), "pasesClave", new BigDecimal("0.10"),
            "regates", new BigDecimal("0.10"), "tackles", new BigDecimal("0.10"),
            "rating", new BigDecimal("0.20"));

    @Override
    public BigDecimal calcular(ResumenRendimiento r, String posicion,
                               Map<String, Integer> maximos, ParametrosCotizacion parametros) {
        BigDecimal score = BigDecimal.ZERO;
        score = score.add(normalizar(r.goles(), maximos.get("goles")).multiply(PESOS.get("goles")));
        score = score.add(normalizar(r.asistencias(), maximos.get("asistencias")).multiply(PESOS.get("asistencias")));
        score = score.add(normalizar(r.tiros(), maximos.get("tiros")).multiply(PESOS.get("tiros")));
        score = score.add(normalizar(r.pasesClave(), maximos.get("pasesClave")).multiply(PESOS.get("pasesClave")));
        score = score.add(normalizar(r.regates(), maximos.get("regates")).multiply(PESOS.get("regates")));
        score = score.add(normalizar(r.tackles(), maximos.get("tackles")).multiply(PESOS.get("tackles")));
        score = score.add(normalizar(r.rating(), maximos.get("rating")).multiply(PESOS.get("rating")));
        return parametros.valorBase().add(score.multiply(parametros.factorEscala())).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizar(int valor, Integer maximo) {
        if (maximo == null || maximo <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(valor).divide(BigDecimal.valueOf(maximo), 8, RoundingMode.HALF_UP)
                .min(BigDecimal.ONE).max(BigDecimal.ZERO);
    }

    private BigDecimal normalizar(BigDecimal valor, Integer maximo) {
        if (maximo == null || maximo <= 0) return BigDecimal.ZERO;
        return valor.divide(BigDecimal.valueOf(maximo), 8, RoundingMode.HALF_UP)
                .min(BigDecimal.ONE).max(BigDecimal.ZERO);
    }
}
