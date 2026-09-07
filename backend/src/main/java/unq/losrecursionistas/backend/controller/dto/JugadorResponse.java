package unq.losrecursionistas.backend.controller.dto;

import unq.losrecursionistas.backend.model.Jugador;

public record JugadorResponse(Long id, String nombre, String liga, String equipo,
		String posicion, boolean activo, boolean disponible) {

	public static JugadorResponse from(Jugador jugador) {
		return new JugadorResponse(jugador.getId(), jugador.getNombre(), jugador.getLiga().getNombre(),
				jugador.getEquipo(), jugador.getPosicion(), jugador.isActivo(), jugador.isDisponible());
	}
}