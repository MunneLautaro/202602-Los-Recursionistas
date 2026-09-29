package unq.losrecursionistas.backend.client.footballdata.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FootballCompetitionTeamsResponse(
    CompetitionDto competition,
    List<FootballTeamResponse> teams
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CompetitionDto(
        Long id,
        String name,
        String code
    ) {}
}
