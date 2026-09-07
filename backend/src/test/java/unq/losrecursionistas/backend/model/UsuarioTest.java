package unq.losrecursionistas.backend.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.Test;

import unq.losrecursionistas.backend.model.Usuario;

class UsuarioTest {

	@Test
	void rechazaSaldoNegativo() {
		Usuario usuario = new Usuario(1L, "ana", "hash", new BigDecimal("-1"), true, Set.of("USER"));
		assertThrows(RuntimeException.class, usuario::validarDatosBasicos);
	}
}