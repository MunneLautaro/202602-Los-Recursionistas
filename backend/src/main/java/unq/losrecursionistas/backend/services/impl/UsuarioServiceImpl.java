package unq.losrecursionistas.backend.services.impl;

import java.math.BigDecimal;
import java.util.Set;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import unq.losrecursionistas.backend.exceptions.CredencialesInvalidasException;
import unq.losrecursionistas.backend.exceptions.UsernameInvalidoException;
import unq.losrecursionistas.backend.exceptions.UsuarioNoEncontradoException;
import unq.losrecursionistas.backend.exceptions.UsuarioYaExisteException;
import unq.losrecursionistas.backend.model.ApiKey;
import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.UsuarioRepository;
import unq.losrecursionistas.backend.security.ApiKeyService;
import unq.losrecursionistas.backend.services.interfaces.UsuarioService;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

	private final UsuarioRepository repository;
	private final ApiKeyService apiKeyService;

	public UsuarioServiceImpl(UsuarioRepository repository, ApiKeyService apiKeyService) {
		this.repository = repository;
		this.apiKeyService = apiKeyService;
	}

	@Override
	public ApiKey crear(String username) {
		String normalized = username == null ? "" : username.trim();
		if (normalized.isBlank()) throw new UsernameInvalidoException();
		if (repository.estaRegistradoElUsername(normalized)) throw new UsuarioYaExisteException(normalized);
		String apiKey = apiKeyService.generar();
		Usuario usuario = new Usuario(normalized, apiKeyService.hash(apiKey),
				new BigDecimal("1000.00"), true, Set.of("USER"));
		Usuario guardado = repository.guardar(usuario);
		return new ApiKey(guardado.getId(), apiKey);
	}

	@Override
	public Usuario autenticar(String username, String apiKey) {
		if (username == null || apiKey == null) throw new CredencialesInvalidasException();
		try {
			Usuario usuario = repository.recuperarPorUsername(username.trim());
			if (!usuario.isHabilitado() || !apiKeyService.coincide(apiKey, usuario.getApiKeyHash())) {
				throw new CredencialesInvalidasException();
			}
			return usuario;
		} catch (UsuarioNoEncontradoException exception) {
			throw new CredencialesInvalidasException();
		}
	}
}