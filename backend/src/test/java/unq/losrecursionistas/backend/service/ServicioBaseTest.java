package unq.losrecursionistas.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;
import unq.losrecursionistas.backend.security.ConfiguracionSeguridad;
import unq.losrecursionistas.backend.service.impl.ServicioBaseImpl;

class ServicioBaseTest {

	@Test
	void elServicioBaseConservaLaEntidadGuardada() {
		ServicioDePrueba servicio = new ServicioDePrueba();

		assertEquals("entidad", servicio.guardar("entidad"));
	}

	@Test
	void elServicioBasePropagaErroresDeValidacion() {
		ServicioDePrueba servicio = new ServicioDePrueba();

		assertThrows(ExcepcionValidacion.class, () -> servicio.guardar(null));
	}

	@Test
	void laConfiguracionDeSeguridadEsUnaConfiguracionIndependiente() {
		assertEquals(Configuration.class,
				ConfiguracionSeguridad.class.getAnnotation(Configuration.class).annotationType());
	}

	private static final class ServicioDePrueba extends ServicioBaseImpl<String> {

		@Override
		public String guardar(String entidad) {
			if (entidad == null || entidad.isBlank()) {
				throw new ExcepcionValidacion("La entidad es obligatoria");
			}
			return entidad;
		}
	}
}