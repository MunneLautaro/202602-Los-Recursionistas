package unq.losrecursionistas.backend.client.footballdata.dto;

public record FootballMatchResponse(
        Long id,
        Competition competition,
        Season season,
        String utcDate,
        String status,
        Integer matchday,
        String stage,
        String group,
        Team homeTeam,
        Team awayTeam,
        Score score
) {
    public record Competition(Long id, String name, String code, String type, String emblem) {}
    public record Season(Long id, String startDate, String endDate, Integer currentMatchday) {}
    public record Team(Long id, String name, String shortName, String tla, String crest) {}
    public record Score(String winner, String duration, FullTime fullTime) {}
    public record FullTime(Integer home, Integer away) {}
}
