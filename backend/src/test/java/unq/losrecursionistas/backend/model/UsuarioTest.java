package unq.losrecursionistas.backend.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import unq.losrecursionistas.backend.exceptions.ExcepcionDominio;

class UsuarioTest {

	@Test
	void permiteDebitarUnMontoDisponible() {
		Usuario usuario = new Usuario("jugador1", "secreto", 100.00);

		usuario.debitar(35.50);

		assertEquals(100.00 - 35.50, usuario.getSaldo());

	}

	@Test
	void rechazaDebitarUnMontoMayorAlSaldo() {
		Usuario usuario = new Usuario("jugador1", "secreto", 10.00);

			assertThrows(ExcepcionDominio.class, () -> usuario.debitar(10.01));

	}

	@Test
	void permiteAcreditarUnMonto() {
		Usuario usuario = new Usuario("jugador1", "secreto", 100.00);

		usuario.acreditar(50.00);

		assertEquals(100.00 + 50.00, usuario.getSaldo());
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