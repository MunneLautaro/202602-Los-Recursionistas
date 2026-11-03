package unq.losrecursionistas.backend.adapter;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.adapter.footballdata.dto.FootballMatchResponse;

import static org.assertj.core.api.Assertions.assertThat;

class FootballMatchResponseTest {

    @Test
    void construyeRecordsAnidadosDelPartido() {
        FootballMatchResponse match = new FootballMatchResponse(1L,
                new FootballMatchResponse.Competition(2L, "Liga", "L", "LEAGUE", "url"),
                new FootballMatchResponse.Season(3L, "2025-01-01", "2025-12-31", 1),
                "2025-01-01T10:00:00Z", "FINISHED", 1, "REGULAR", null,
                new FootballMatchResponse.Team(4L, "Local", "L", "LOC", "url"),
                new FootballMatchResponse.Team(5L, "Visitante", "V", "VIS", "url"),
                new FootballMatchResponse.Score("HOME", "REGULAR",
                        new FootballMatchResponse.FullTime(2, 1)));

        assertThat(match.score().fullTime().home()).isEqualTo(2);
        assertThat(match.homeTeam().name()).isEqualTo("Local");
    }
}
