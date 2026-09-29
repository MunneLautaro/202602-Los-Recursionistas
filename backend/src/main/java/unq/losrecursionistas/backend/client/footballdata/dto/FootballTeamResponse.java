package unq.losrecursionistas.backend.client.footballdata.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FootballTeamResponse(
    Long id,
    String name,
    String shortName,
    String tla,
    String crest,
    List<FootballPlayerResponse> squad) {
}