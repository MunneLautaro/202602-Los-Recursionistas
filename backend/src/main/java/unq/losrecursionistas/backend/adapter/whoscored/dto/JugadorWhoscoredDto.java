package unq.losrecursionistas.backend.adapter.whoscored.dto;

import java.time.LocalDate;
import java.util.List;

public record JugadorWhoscoredDto(
        Long idWhoscored,
        String nombre,
        String slug,
        LocalDate fechaNacimiento,
        String nacionalidad,
        Long equipoWhoscoredId,
        String equipoNombre,
        List<String> posiciones) {

    public JugadorWhoscoredDto {
        posiciones = posiciones == null ? List.of() : List.copyOf(posiciones);
    }
}
