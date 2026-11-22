package unq.losrecursionistas.backend.model;

import java.math.BigDecimal;

/**
 * Posición de un jugador en el ranking de cotizaciones: valor vigente, valor anterior y su variación.
 */
public record RankingCotizacion(Long jugadorId, String nombre, String posicion, String equipo,
                                String liga, BigDecimal valorActual, BigDecimal valorAnterior,
                                BigDecimal variacion) {
}
