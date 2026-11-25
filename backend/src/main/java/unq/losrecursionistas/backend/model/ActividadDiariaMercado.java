package unq.losrecursionistas.backend.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Actividad de mercado agregada por dia (zona America/Argentina/Buenos_Aires).
 * Es un read-model de solo lectura; no se persiste.
 */
public record ActividadDiariaMercado(LocalDate dia, long ordenesCompra, long ordenesVenta,
                                     long emparejamientos, BigDecimal volumenCreditos) {

    public static ActividadDiariaMercado vacia(LocalDate dia) {
        return new ActividadDiariaMercado(dia, 0, 0, 0, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }
}
