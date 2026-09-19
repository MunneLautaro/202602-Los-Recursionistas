package unq.losrecursionistas.backend.model;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import unq.losrecursionistas.backend.exceptions.ExcepcionDominio;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;
import unq.losrecursionistas.backend.security.ManejadorToken;
import unq.losrecursionistas.backend.security.PoliticaAcceso;

class ModeloBaseTest {

	@Test
	void lasExcepcionesDeDominioYValidacionSonRuntime() {
		assertTrue(RuntimeException.class.isAssignableFrom(ExcepcionDominio.class));
		assertTrue(RuntimeException.class.isAssignableFrom(ExcepcionValidacion.class));
	}

	@Test
	void losContratosDeSeguridadNoSeMezclanConElModelo() {
		assertTrue(ManejadorToken.class.isInterface());
		assertTrue(PoliticaAcceso.class.isInterface());
	}

}