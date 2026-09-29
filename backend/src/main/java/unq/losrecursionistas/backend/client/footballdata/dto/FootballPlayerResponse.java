package unq.losrecursionistas.backend.client.footballdata.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FootballPlayerResponse(
    Long id,
    String name,
    String lastName,
    String position,
    String dateOfBirth,
    String nationality
) {
}