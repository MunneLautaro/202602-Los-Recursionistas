package unq.losrecursionistas.backend.controller.dto.cotizacion;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.model.RankingCotizacion;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class RankingJugadorResponseTest {

    @Test
    void desdeRankingCopiaTodosLosCampos() {
        RankingCotizacion ranking = new RankingCotizacion(5L, "Salah", "Delantero", "Liverpool",
                "Premier League", new BigDecimal("120.00"), new BigDecimal("100.00"), new BigDecimal("20.00"));

        RankingJugadorResponse respuesta = RankingJugadorResponse.desde(ranking);

        assertThat(respuesta).isEqualTo(new RankingJugadorResponse(5L, "Salah", "Delantero", "Liverpool",
                "Premier League", new BigDecimal("120.00"), new BigDecimal("100.00"), new BigDecimal("20.00")));
    }
}
