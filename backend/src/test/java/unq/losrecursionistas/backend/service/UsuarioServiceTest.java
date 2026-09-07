package unq.losrecursionistas.backend.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import unq.losrecursionistas.backend.security.ApiKeyService;
import unq.losrecursionistas.backend.exceptions.DomainException;
import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.UsuarioRepository;
import unq.losrecursionistas.backend.services.impl.UsuarioServiceImpl;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

	@Mock
	private UsuarioRepository repository;

	private final ApiKeyService apiKeyService = new ApiKeyService();

	@Test
	void creaApiKeyYNoLaRepite() {
		when(repository.estaRegistradoElUsername(anyString())).thenReturn(false);
		when(repository.guardar(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
		UsuarioServiceImpl service = new UsuarioServiceImpl(repository, apiKeyService);

		var first = service.crear("ana");
		var second = service.crear("juan");
		assertNotEquals(first.apiKey(), second.apiKey());
	}

	@Test
	void rechazaApiKeyInvalida() {
		when(repository.recuperarPorUsername("ana"))
				.thenReturn(new Usuario("ana", apiKeyService.hash("correcta"),
					new java.math.BigDecimal("1000.00"), true, java.util.Set.of("USER")));
		UsuarioServiceImpl service = new UsuarioServiceImpl(repository, apiKeyService);
		assertThrows(DomainException.class, () -> service.autenticar("ana", "incorrecta"));
	}
}