package unq.losrecursionistas.backend.service.impl.estrategia;

import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.service.interfaces.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.Map;

@Component("PERFORMANCE_POR_POSICION")
public class PerformancePorPosicionEstrategia implements EstrategiaCotizacion {
    private static final String PORTERO = "portero";
    private static final String DEFENSOR = "defensor";
    private static final String MEDIOCAMPISTA = "mediocampista";
    private static final String DELANTERO = "delantero";
    private static final String GOLES = "goles";
    private static final String ASISTENCIAS = "asistencias";
    private static final String TIROS = "tiros";
    private static final String PASES_CLAVE = "pasesClave";
    private static final String REGATES = "regates";
    private static final String TACKLES = "tackles";
    private static final String INTERCEPCIONES = "intercepciones";
    private static final String MINUTOS = "minutos";
    private static final String RATING = "rating";
    private static final BigDecimal PESO_35 = new BigDecimal("0.35");
    private static final BigDecimal PESO_30 = new BigDecimal("0.30");
    private static final BigDecimal PESO_25 = new BigDecimal("0.25");
    private static final BigDecimal PESO_20 = new BigDecimal("0.20");
    private static final BigDecimal PESO_15 = new BigDecimal("0.15");
    private static final BigDecimal PESO_10 = new BigDecimal("0.10");

    private static final Map<String, Map<String, BigDecimal>> PESOS = Map.of(
            PORTERO, Map.of(INTERCEPCIONES, PESO_30, MINUTOS, PESO_30, RATING, PESO_20),
            DEFENSOR, Map.of(TACKLES, PESO_30, INTERCEPCIONES, PESO_20, RATING, PESO_20),
            MEDIOCAMPISTA, Map.of(PASES_CLAVE, PESO_20, ASISTENCIAS, PESO_20, RATING, PESO_20),
            DELANTERO, Map.of(GOLES, PESO_35, TIROS, PESO_20, RATING, PESO_20));
    private static final Map<String, BigDecimal> PESOS_GENERALES = Map.of(
            GOLES, PESO_25, ASISTENCIAS, PESO_15,
            TIROS, PESO_10, PASES_CLAVE, PESO_10,
            REGATES, PESO_10, TACKLES, PESO_10,
            RATING, PESO_20);

    @Override
    public BigDecimal calcular(ResumenRendimiento r, String posicion,
                               Map<String, Integer> maximos, ParametrosCotizacion parametros) {
        String categoria = categoria(posicion);
        Map<String, BigDecimal> pesos = PESOS.getOrDefault(categoria, PESOS_GENERALES);
        BigDecimal score = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> peso : pesos.entrySet()) {
            int valor = switch (peso.getKey()) {
                case GOLES -> r.goles();
                case ASISTENCIAS -> r.asistencias();
                case TIROS -> r.tiros();
                case PASES_CLAVE -> r.pasesClave();
                case TACKLES -> r.tackles();
                case INTERCEPCIONES -> r.intercepciones();
                case MINUTOS -> r.minutos();
                case RATING -> r.rating().intValue();
                default -> 0;
            };
            score = score.add(normalizar(valor, maximos.get(peso.getKey())).multiply(peso.getValue()));
        }
        return parametros.valorBase().add(score.multiply(parametros.factorEscala())).setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizarPosicion(String posicion) {
        if (posicion == null) return "";
        return Normalizer.normalize(posicion, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase().replaceAll("[^a-z]", "");
    }

    private String categoria(String posicion) {
        String p = normalizarPosicion(posicion);
        if (p.contains(PORTERO) || p.contains("goalkeeper")) return PORTERO;
        if (p.contains(DEFENSOR) || p.contains("defender")) return DEFENSOR;
        if (p.contains(DELANTERO) || p.contains("forward") || p.contains("striker")) return DELANTERO;
        if (p.contains("medio") || p.contains("midfielder")) return MEDIOCAMPISTA;
        return "";
    }

    private BigDecimal normalizar(int valor, Integer maximo) {
        if (maximo == null || maximo <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(valor).divide(BigDecimal.valueOf(maximo), 8, RoundingMode.HALF_UP)
                .min(BigDecimal.ONE).max(BigDecimal.ZERO);
    }
}
