package unq.losrecursionistas.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import unq.losrecursionistas.backend.controller.dto.AltaUsuarioRequest;
import unq.losrecursionistas.backend.controller.dto.AltaUsuarioResponse;
import unq.losrecursionistas.backend.controller.dto.TokenRequest;
import unq.losrecursionistas.backend.controller.dto.TokenResponse;
import unq.losrecursionistas.backend.model.ApiKey;
import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.security.JwtService;
import unq.losrecursionistas.backend.services.interfaces.UsuarioService;

@RestController
@RequestMapping
public class UsuarioController {

	private final UsuarioService service;
	private final JwtService jwtService;

	public UsuarioController(UsuarioService service, JwtService jwtService) {
		this.service = service;
		this.jwtService = jwtService;
	}

	@PostMapping("/users")
	public ResponseEntity<AltaUsuarioResponse> crear(@Valid @RequestBody AltaUsuarioRequest request) {
		ApiKey apiKey = service.crear(request.username());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new AltaUsuarioResponse(apiKey.getUsuarioId(), request.username().trim(), apiKey.getValor()));
	}

	@PostMapping("/auth/token")
	public ResponseEntity<TokenResponse> token(@Valid @RequestBody TokenRequest request) {
		Usuario usuario = service.autenticar(request.username(), request.apiKey());
		String rol = usuario.getRoles().stream().findFirst().orElse("USER");
		return ResponseEntity.ok(new TokenResponse(jwtService.generate(usuario.getUsername(), rol), "Bearer"));
	}
}