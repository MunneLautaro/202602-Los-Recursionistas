package unq.losrecursionistas.backend.service.interfaces;

import java.math.BigDecimal;
import java.util.Map;

public record ParametrosCotizacion(BigDecimal valorBase, BigDecimal factorEscala,
                                   Map<String, BigDecimal> pesos) {
    public ParametrosCotizacion {
        if (valorBase == null || factorEscala == null || pesos == null) {
            throw new IllegalArgumentException("La configuracion de cotizacion esta incompleta");
        }
        pesos = Map.copyOf(pesos);
    }
}
