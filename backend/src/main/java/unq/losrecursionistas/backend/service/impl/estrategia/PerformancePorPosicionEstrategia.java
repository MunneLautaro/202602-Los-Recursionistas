package unq.losrecursionistas.backend.service.impl.estrategia;

import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.service.interfaces.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.Map;

@Component("PERFORMANCE_POR_POSICION")
public class PerformancePorPosicionEstrategia implements EstrategiaCotizacion {
    private static final Map<String, Map<String, BigDecimal>> PESOS = Map.of(
            "portero", Map.of("intercepciones", new BigDecimal("0.30"), "minutos", new BigDecimal("0.30"), "rating", new BigDecimal("0.20")),
            "defensor", Map.of("tackles", new BigDecimal("0.30"), "intercepciones", new BigDecimal("0.20"), "rating", new BigDecimal("0.20")),
            "mediocampista", Map.of("pasesClave", new BigDecimal("0.20"), "asistencias", new BigDecimal("0.20"), "rating", new BigDecimal("0.20")),
            "delantero", Map.of("goles", new BigDecimal("0.35"), "tiros", new BigDecimal("0.20"), "rating", new BigDecimal("0.20")));
    private static final Map<String, BigDecimal> PESOS_GENERALES = Map.of(
            "goles", new BigDecimal("0.25"), "asistencias", new BigDecimal("0.15"),
            "tiros", new BigDecimal("0.10"), "pasesClave", new BigDecimal("0.10"),
            "regates", new BigDecimal("0.10"), "tackles", new BigDecimal("0.10"),
            "rating", new BigDecimal("0.20"));

    @Override
    public BigDecimal calcular(ResumenRendimiento r, String posicion,
                               Map<String, Integer> maximos, ParametrosCotizacion parametros) {
        String categoria = categoria(posicion);
        Map<String, BigDecimal> pesos = PESOS.getOrDefault(categoria, PESOS_GENERALES);
        BigDecimal score = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> peso : pesos.entrySet()) {
            int valor = switch (peso.getKey()) {
                case "goles" -> r.goles();
                case "asistencias" -> r.asistencias();
                case "tiros" -> r.tiros();
                case "pasesClave" -> r.pasesClave();
                case "tackles" -> r.tackles();
                case "intercepciones" -> r.intercepciones();
                case "minutos" -> r.minutos();
                case "rating" -> r.rating().intValue();
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
        if (p.contains("portero") || p.contains("goalkeeper")) return "portero";
        if (p.contains("defensor") || p.contains("defender")) return "defensor";
        if (p.contains("delantero") || p.contains("forward") || p.contains("striker")) return "delantero";
        if (p.contains("medio") || p.contains("midfielder")) return "mediocampista";
        return "";
    }

    private BigDecimal normalizar(int valor, Integer maximo) {
        if (maximo == null || maximo <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(valor).divide(BigDecimal.valueOf(maximo), 8, RoundingMode.HALF_UP)
                .min(BigDecimal.ONE).max(BigDecimal.ZERO);
    }
}
