package unq.losrecursionistas.backend.security;

import java.math.BigDecimal;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

import unq.losrecursionistas.backend.model.Usuario;

@TestConfiguration(proxyBeanMethods = false)
public class SeguridadTestConfig {

	public static Usuario crearUsuarioPrueba(PasswordEncoder passwordEncoder) {
		return new Usuario("jugador-prueba", passwordEncoder.encode("contrasena-prueba"), BigDecimal.ZERO);
	}
}