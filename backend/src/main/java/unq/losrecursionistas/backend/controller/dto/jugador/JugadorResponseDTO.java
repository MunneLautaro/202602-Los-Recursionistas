package unq.losrecursionistas.backend.controller.dto.jugador;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import unq.losrecursionistas.backend.model.Equipo;
import unq.losrecursionistas.backend.model.Jugador;

import java.time.LocalDate;

public record JugadorResponseDTO(

        Long id,
        Long idExterno,
        String nombre,
        String posicion,
        String nacionalidad,
        LocalDate fechaNacimiento,
        Equipo equipo

) {
    public static JugadorResponseDTO desdeModelo(Jugador jugador) {
        return new JugadorResponseDTO(
            jugador.getId(),
            jugador.getIdExterno(),
            jugador.getNombre(),
            jugador.getPosicion(),
            jugador.getNacionalidad(),
            jugador.getFechaNacimiento(),
            jugador.getEquipo()
        );
    }
}
