package unq.losrecursionistas.backend.controller.dto.jugador;

import unq.losrecursionistas.backend.controller.dto.equipo.EquipoResponseDTO;
import unq.losrecursionistas.backend.model.Jugador;

import java.time.LocalDate;

public record JugadorResponseDTO(

        Long id,
        Long idExterno,
        String nombre,
        String posicion,
        String nacionalidad,
        LocalDate fechaNacimiento,
        EquipoResponseDTO equipo

) {
    public static JugadorResponseDTO desdeModelo(Jugador jugador) {
        return new JugadorResponseDTO(
            jugador.getId(),
            jugador.getIdExterno(),
            jugador.getNombre(),
            jugador.getPosicion(),
            jugador.getNacionalidad(),
            jugador.getFechaNacimiento(),
            EquipoResponseDTO.desdeModelo(jugador.getEquipo())
        );
    }
}
