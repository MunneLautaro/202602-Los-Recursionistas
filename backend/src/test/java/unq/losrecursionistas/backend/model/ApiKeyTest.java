package unq.losrecursionistas.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import unq.losrecursionistas.backend.model.ApiKey;

class ApiKeyTest {

	@Test
	void conservaSoloLaIdentidadYElValorEntregado() {
		ApiKey apiKey = new ApiKey(1L, "valor-unico");
		assertEquals(1L, apiKey.usuarioId());
		assertEquals("valor-unico", apiKey.valor());
	}
}