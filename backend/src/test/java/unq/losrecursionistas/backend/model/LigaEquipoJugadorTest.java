package unq.losrecursionistas.backend.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;

class LigaEquipoJugadorTest {

	@Test
	void creaLasRelacionesObligatoriasDelDominio() {
		Liga liga = new Liga("Primera Division", "ARG");
		Equipo equipo = new Equipo("Equipo Uno", "EQU", liga);
		Jugador jugador = new Jugador("Jugador Uno", "DELANTERO", equipo, true);

		assertThat(liga.getNombre()).isEqualTo("Primera Division");
		assertThat(equipo.getLiga()).isSameAs(liga);
		assertThat(jugador.getEquipo()).isSameAs(equipo);
		assertThat(jugador.isActivo()).isTrue();
	}

	@Test
	void rechazaNombresYCodigosVacios() {
		assertThatThrownBy(() -> new Liga("", "ARG"))
				.isInstanceOf(ExcepcionValidacion.class);
		assertThatThrownBy(() -> new Liga("Primera", ""))
				.isInstanceOf(ExcepcionValidacion.class);
		assertThatThrownBy(() -> new Equipo("", "EQU", new Liga("Primera", "ARG")))
				.isInstanceOf(ExcepcionValidacion.class);
		assertThatThrownBy(() -> new Jugador("", "DELANTERO",
				new Equipo("Equipo", "EQU", new Liga("Primera", "ARG")), true))
				.isInstanceOf(ExcepcionValidacion.class);
	}

	@Test
	void rechazaRelacionesObligatoriasAusentes() {
		assertThatThrownBy(() -> new Equipo("Equipo", "EQU", null))
				.isInstanceOf(ExcepcionValidacion.class);
		assertThatThrownBy(() -> new Jugador("Jugador", "DELANTERO", null, true))
				.isInstanceOf(ExcepcionValidacion.class);
	}
}