package unq.losrecursionistas.backend.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import unq.losrecursionistas.backend.exceptions.ExcepcionDominio;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;

class UsuarioTest {

	@Test
	void creaUnUsuarioConSaldoNoNegativoYCamposObligatorios() {
		Usuario usuario = new Usuario("jugador1", "secreto", new BigDecimal("100.00"));

		assertThat(usuario.getNombreUsuario()).isEqualTo("jugador1");
		assertThat(usuario.getContrasena()).isEqualTo("secreto");
		assertThat(usuario.getSaldo()).isEqualByComparingTo("100.00");
	}

	@Test
	void rechazaSaldoInicialNegativo() {
		assertThatThrownBy(() -> new Usuario("jugador1", "secreto", new BigDecimal("-0.01")))
				.isInstanceOf(ExcepcionValidacion.class);
	}

	@Test
	void permiteDebitarUnMontoDisponible() {
		Usuario usuario = new Usuario("jugador1", "secreto", new BigDecimal("100.00"));

		usuario.debitar(new BigDecimal("35.50"));

		assertThat(usuario.getSaldo()).isEqualByComparingTo("64.50");
	}

	@Test
	void rechazaDebitarUnMontoMayorAlSaldo() {
		Usuario usuario = new Usuario("jugador1", "secreto", new BigDecimal("10.00"));

		assertThatThrownBy(() -> usuario.debitar(new BigDecimal("10.01")))
				.isInstanceOf(ExcepcionDominio.class);
	}

	@Test
	void rechazaCamposObligatoriosVacios() {
		assertThatThrownBy(() -> new Usuario("", "secreto", BigDecimal.ZERO))
				.isInstanceOf(ExcepcionValidacion.class);
		assertThatThrownBy(() -> new Usuario("jugador1", "", BigDecimal.ZERO))
				.isInstanceOf(ExcepcionValidacion.class);
	}
}