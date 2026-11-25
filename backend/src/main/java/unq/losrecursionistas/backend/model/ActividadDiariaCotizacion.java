package unq.losrecursionistas.backend.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cotizaciones calculadas por dia y su variacion promedio respecto del valor previo
 * de cada jugador (fraccion; null si ninguna cotizacion del dia tiene previo).
 * {@code conPrevio} es la cantidad de cotizaciones del dia con valor previo positivo,
 * usada para ponderar el promedio del periodo. Read-model de solo lectura.
 */
public record ActividadDiariaCotizacion(LocalDate dia, long jugadoresCotizados,
                                        BigDecimal variacionPromedio, long conPrevio) {

    public static ActividadDiariaCotizacion vacia(LocalDate dia) {
        return new ActividadDiariaCotizacion(dia, 0, null, 0);
    }
}
