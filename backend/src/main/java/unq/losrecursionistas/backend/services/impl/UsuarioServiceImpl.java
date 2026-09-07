package unq.losrecursionistas.backend.services.impl;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import unq.losrecursionistas.backend.security.JwtService;
import unq.losrecursionistas.backend.controller.dto.AltaUsuarioResponse;
import unq.losrecursionistas.backend.controller.dto.TokenResponse;
import unq.losrecursionistas.backend.exceptions.DomainException;
import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.UsuarioRepository;
import unq.losrecursionistas.backend.services.interfaces.UsuarioService;

@Service
public class UsuarioServiceImpl implements UsuarioService {

	private final UsuarioRepository repository;
	private final JwtService jwtService;
	private final AtomicLong sequence = new AtomicLong();

	public UsuarioServiceImpl(UsuarioRepository repository, JwtService jwtService) {
		this.repository = repository;
		this.jwtService = jwtService;
	}

	@Override
	public AltaUsuarioResponse crear(String username) {
		String normalized = username == null ? "" : username.trim();
		if (normalized.isBlank()) throw new DomainException("VALIDACION_INVALIDA", "El username es obligatorio");
		if (repository.existsByUsername(normalized)) throw new DomainException("CONFLICTO", "El username ya existe");
		String apiKey = UUID.randomUUID().toString();
		Usuario usuario = new Usuario(sequence.incrementAndGet(), normalized, hash(apiKey),
				new BigDecimal("1000.00"), true, Set.of("USER"));
		repository.save(usuario);
		return new AltaUsuarioResponse(usuario.id(), usuario.username(), apiKey);
	}

	@Override
	public TokenResponse emitirToken(String username, String apiKey) {
		Usuario usuario = repository.findByUsername(username.trim())
				.filter(value -> value.habilitado() && value.apiKeyHash().equals(hash(apiKey)))
				.orElseThrow(() -> new DomainException("CREDENCIALES_INVALIDAS", "Las credenciales no son validas"));
		return new TokenResponse(jwtService.generate(usuario.username(), usuario.roles().iterator().next()), "Bearer");
	}

	private String hash(String value) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
					.digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("No se pudo proteger la ApiKey", exception);
		}
	}
}