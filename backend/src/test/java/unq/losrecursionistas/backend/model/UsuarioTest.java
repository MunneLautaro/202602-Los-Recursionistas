package unq.losrecursionistas.backend.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import unq.losrecursionistas.backend.exceptions.ExcepcionDominio;

class UsuarioTest {

	@Test
	void permiteDebitarUnMontoDisponible() {
		Usuario usuario = new Usuario("jugador1", "secreto", 100.00);

		usuario.debitar(35.50);

		assertThat(usuario.getSaldo()).isEqualByComparingTo(100.00 - 35.50);
	}

	@Test
	void rechazaDebitarUnMontoMayorAlSaldo() {
		Usuario usuario = new Usuario("jugador1", "secreto", 10.00);

		assertThatThrownBy(() -> usuario.debitar(10.01))
				.isInstanceOf(ExcepcionDominio.class);
	}

	@Test
	void permiteAcreditarUnMonto() {
		Usuario usuario = new Usuario("jugador1", "secreto", 100.00);

		usuario.acreditar(50.00);

		assertThat(usuario.getSaldo()).isEqualByComparingTo(100.00 + 50.00);
	}

	@Test
	void permiteCambiarEstadoHabilitado() {
		Usuario usuario = new Usuario("jugador1", "secreto", 100.00);

		usuario.deshabilitar();
		assertThat(usuario.isHabilitado()).isFalse();

		usuario.habilitar();
		assertThat(usuario.isHabilitado()).isTrue();
	}
}