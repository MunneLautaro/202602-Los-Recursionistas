package unq.losrecursionistas.backend.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import unq.losrecursionistas.backend.exceptions.ExcepcionDominio;

class UsuarioTest {

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
	void permiteAcreditarUnMonto() {
		Usuario usuario = new Usuario("jugador1", "secreto", new BigDecimal("100.00"));

		usuario.acreditar(new BigDecimal("50.00"));

		assertThat(usuario.getSaldo()).isEqualByComparingTo("150.00");
	}

	@Test
	void permiteCambiarEstadoHabilitado() {
		Usuario usuario = new Usuario("jugador1", "secreto", new BigDecimal("100.00"));

		usuario.deshabilitar();
		assertThat(usuario.isHabilitado()).isFalse();

		usuario.habilitar();
		assertThat(usuario.isHabilitado()).isTrue();
	}
}