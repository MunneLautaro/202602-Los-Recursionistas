package unq.losrecursionistas.backend.controller.dto.cotizacion;

import unq.losrecursionistas.backend.model.RankingCotizacion;

import java.math.BigDecimal;

public record RankingJugadorResponse(Long jugadorId, String nombre, String posicion,
                                     String equipo, String liga, BigDecimal valorActual,
                                     BigDecimal valorAnterior, BigDecimal variacion) {
    public static RankingJugadorResponse desde(RankingCotizacion ranking) {
        return new RankingJugadorResponse(ranking.jugadorId(), ranking.nombre(), ranking.posicion(),
                ranking.equipo(), ranking.liga(), ranking.valorActual(), ranking.valorAnterior(),
                ranking.variacion());
    }
}
