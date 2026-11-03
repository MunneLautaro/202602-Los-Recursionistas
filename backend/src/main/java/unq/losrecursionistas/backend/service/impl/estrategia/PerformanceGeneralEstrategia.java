package unq.losrecursionistas.backend.service.impl.estrategia;

import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.service.interfaces.EstrategiaCotizacion;
import unq.losrecursionistas.backend.service.interfaces.ParametrosCotizacion;
import unq.losrecursionistas.backend.service.interfaces.ResumenRendimiento;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Component("performanceGeneralEstrategia")
public class PerformanceGeneralEstrategia implements EstrategiaCotizacion {
    private static final String GOLES = "goles";
    private static final String ASISTENCIAS = "asistencias";
    private static final String TIROS = "tiros";
    private static final String PASES_CLAVE = "pasesClave";
    private static final String REGATES = "regates";
    private static final String TACKLES = "tackles";
    private static final String RATING = "rating";
    private static final BigDecimal PESO_25 = new BigDecimal("0.25");
    private static final BigDecimal PESO_15 = new BigDecimal("0.15");
    private static final BigDecimal PESO_10 = new BigDecimal("0.10");
    private static final BigDecimal PESO_20 = new BigDecimal("0.20");

    private static final Map<String, BigDecimal> PESOS = Map.of(
            GOLES, PESO_25, ASISTENCIAS, PESO_15,
            TIROS, PESO_10, PASES_CLAVE, PESO_10,
            REGATES, PESO_10, TACKLES, PESO_10,
            RATING, PESO_20);

    @Override
    public BigDecimal calcular(ResumenRendimiento r, String posicion,
                               Map<String, Integer> maximos, ParametrosCotizacion parametros) {
        BigDecimal score = BigDecimal.ZERO;
        score = score.add(normalizar(r.goles(), maximos.get(GOLES)).multiply(PESOS.get(GOLES)));
        score = score.add(normalizar(r.asistencias(), maximos.get(ASISTENCIAS)).multiply(PESOS.get(ASISTENCIAS)));
        score = score.add(normalizar(r.tiros(), maximos.get(TIROS)).multiply(PESOS.get(TIROS)));
        score = score.add(normalizar(r.pasesClave(), maximos.get(PASES_CLAVE)).multiply(PESOS.get(PASES_CLAVE)));
        score = score.add(normalizar(r.regates(), maximos.get(REGATES)).multiply(PESOS.get(REGATES)));
        score = score.add(normalizar(r.tackles(), maximos.get(TACKLES)).multiply(PESOS.get(TACKLES)));
        score = score.add(normalizar(r.rating(), maximos.get(RATING)).multiply(PESOS.get(RATING)));
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
