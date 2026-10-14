package unq.losrecursionistas.backend.adapter.whoscored.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record AparicionWhoscoredDto(
        Long partidoWhoscoredId,
        LocalDate fechaPartido,
        String torneo,
        Long torneoWhoscoredId,
        Long equipoWhoscoredId,
        String equipoNombre,
        Long rivalWhoscoredId,
        String rivalNombre,
        String localVisitante,
        Integer minutos,
        BigDecimal rating,
        Map<String, Object> statsCrudas) {

    public AparicionWhoscoredDto {
        statsCrudas = statsCrudas == null ? Map.of() : Map.copyOf(statsCrudas);
    }
}
