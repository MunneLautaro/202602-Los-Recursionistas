package unq.losrecursionistas.backend.service.interfaces;

import java.math.BigDecimal;
import java.util.Map;

public interface EstrategiaCotizacion {
    BigDecimal calcular(ResumenRendimiento resumen, String posicion,
                        Map<String, Integer> maximos, ParametrosCotizacion parametros);
}
