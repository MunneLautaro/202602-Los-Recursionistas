package unq.losrecursionistas.backend.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import unq.losrecursionistas.backend.security.JwtService;
import unq.losrecursionistas.backend.exceptions.DomainException;
import unq.losrecursionistas.backend.persistence.repository.impl.UsuarioRepositoryImpl;
import unq.losrecursionistas.backend.services.impl.UsuarioServiceImpl;

class UsuarioServiceTest {

	private final UsuarioServiceImpl service = new UsuarioServiceImpl(new UsuarioRepositoryImpl(),
			new JwtService("test-secret", 3_600_000));

	@Test
	void creaApiKeyYNoLaRepite() {
		var first = service.crear("ana");
		var second = service.crear("juan");
		assertNotEquals(first.apiKey(), second.apiKey());
	}

	@Test
	void rechazaApiKeyInvalida() {
		service.crear("ana");
		assertThrows(DomainException.class, () -> service.emitirToken("ana", "incorrecta"));
	}
}