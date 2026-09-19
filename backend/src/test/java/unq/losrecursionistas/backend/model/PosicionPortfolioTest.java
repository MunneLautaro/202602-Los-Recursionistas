package unq.losrecursionistas.backend.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import unq.losrecursionistas.backend.exceptions.ExcepcionDominio;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;

class PosicionPortfolioTest {

	@Test
	void creaUnaPosicionConCantidadYPrecioPositivos() {
		PosicionPortfolio posicion = new PosicionPortfolio(usuarioConSaldo(), jugadorActivo(),
				new BigDecimal("2.00"), new BigDecimal("10.00"));

		assertThat(posicion.getCantidadTokens()).isEqualByComparingTo("2.00");
		assertThat(posicion.getPrecioCompra()).isEqualByComparingTo("10.00");
	}

	@Test
	void rechazaCantidadOPrecioNoPositivos() {
		assertThatThrownBy(() -> new PosicionPortfolio(usuarioConSaldo(), jugadorActivo(), BigDecimal.ZERO,
				BigDecimal.TEN)).isInstanceOf(ExcepcionValidacion.class);
		assertThatThrownBy(() -> new PosicionPortfolio(usuarioConSaldo(), jugadorActivo(), BigDecimal.ONE,
				BigDecimal.ZERO)).isInstanceOf(ExcepcionValidacion.class);
	}

	@Test
	void rechazaCompraSiNoHaySaldoSuficiente() {
		PosicionPortfolio posicion = new PosicionPortfolio(usuarioConSaldo(), jugadorActivo(), BigDecimal.ONE,
				BigDecimal.TEN);

		assertThatThrownBy(() -> posicion.comprar(new BigDecimal("2.00"), new BigDecimal("10.00")))
				.isInstanceOf(ExcepcionDominio.class);
	}

	@Test
	void rechazaVentaSiNoHayTokensSuficientes() {
		PosicionPortfolio posicion = new PosicionPortfolio(usuarioConSaldo(), jugadorActivo(), BigDecimal.ONE,
				BigDecimal.TEN);

		assertThatThrownBy(() -> posicion.vender(new BigDecimal("1.01")))
				.isInstanceOf(ExcepcionDominio.class);
 	}

	@Test
	void rechazaUnaPosicionParaUnJugadorInactivo() {
		assertThatThrownBy(() -> new PosicionPortfolio(usuarioConSaldo(), jugadorInactivo(), BigDecimal.ONE,
				BigDecimal.TEN)).isInstanceOf(ExcepcionDominio.class);
	}

	private Usuario usuarioConSaldo() {
		return new Usuario("jugador1", "secreto", new BigDecimal("10.00"));
	}

	private Jugador jugadorActivo() {
		return jugador(true);
	}

	private Jugador jugadorInactivo() {
		return jugador(false);
	}

	private Jugador jugador(boolean activo) {
		Liga liga = new Liga("Primera", "ARG");
		Equipo equipo = new Equipo("Equipo", "EQU", liga);
		return new Jugador("Jugador", "DELANTERO", equipo, activo);
	}
}