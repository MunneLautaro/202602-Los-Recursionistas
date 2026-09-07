package unq.losrecursionistas.backend.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import unq.losrecursionistas.backend.model.Jugador;

class JugadorTest {

	@Test
	void exigeNombreYLiga() {
		Jugador jugador = new Jugador(1L, " ", "Equipo", "DELANTERO", null, true, true);
		assertThrows(RuntimeException.class, jugador::validarDatosBasicos);
	}
}