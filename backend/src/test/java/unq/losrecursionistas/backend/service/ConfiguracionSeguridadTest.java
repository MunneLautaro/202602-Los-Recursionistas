package unq.losrecursionistas.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

import unq.losrecursionistas.backend.security.ConfiguracionSeguridad;

class ConfiguracionSeguridadTest {

	@Test
	void laConfiguracionDeSeguridadEsUnaConfiguracionIndependiente() {
		assertEquals(Configuration.class,
				ConfiguracionSeguridad.class.getAnnotation(Configuration.class).annotationType());
	}
}