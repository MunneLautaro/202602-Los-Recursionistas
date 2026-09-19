package unq.losrecursionistas.backend.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;

class CotizacionJugadorTest {

	@Test
	void creaUnaCotizacionConValorPositivoYTokensNoNegativos() {
		Jugador jugador = jugadorActivo();
		CotizacionJugador cotizacion = new CotizacionJugador(jugador, new BigDecimal("12.50"),
				new BigDecimal("100.00"));

		assertThat(cotizacion.getJugador()).isSameAs(jugador);
		assertThat(cotizacion.getValor()).isEqualByComparingTo("12.50");
		assertThat(cotizacion.getTokensDisponibles()).isEqualByComparingTo("100.00");
		assertThat(cotizacion.getFechaCotizacion()).isNotNull();
	}

	@Test
	void rechazaValorNoPositivoOTokensNegativos() {
		Jugador jugador = jugadorActivo();

		assertThatThrownBy(() -> new CotizacionJugador(jugador, BigDecimal.ZERO, BigDecimal.ONE))
				.isInstanceOf(ExcepcionValidacion.class);
		assertThatThrownBy(() -> new CotizacionJugador(jugador, BigDecimal.ONE, new BigDecimal("-1")))
				.isInstanceOf(ExcepcionValidacion.class);
	}

	@Test
	void permiteActualizarUnaCotizacionValida() {
		CotizacionJugador cotizacion = new CotizacionJugador(jugadorActivo(), new BigDecimal("12.50"),
				new BigDecimal("100.00"));

		cotizacion.actualizar(new BigDecimal("14.25"), new BigDecimal("80.00"));

		assertThat(cotizacion.getValor()).isEqualByComparingTo("14.25");
		assertThat(cotizacion.getTokensDisponibles()).isEqualByComparingTo("80.00");
	}

	private Jugador jugadorActivo() {
		Liga liga = new Liga("Primera", "ARG");
		Equipo equipo = new Equipo("Equipo", "EQU", liga);
		return new Jugador("Jugador", "DELANTERO", equipo, true);
	}
}