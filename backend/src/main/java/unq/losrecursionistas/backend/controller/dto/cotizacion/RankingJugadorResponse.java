package unq.losrecursionistas.backend.controller.dto.cotizacion;

import java.math.BigDecimal;

public record RankingJugadorResponse(Long jugadorId, String nombre, String posicion,
                                     String equipo, String liga, BigDecimal valorActual,
                                     BigDecimal valorAnterior, BigDecimal variacion) {
}
