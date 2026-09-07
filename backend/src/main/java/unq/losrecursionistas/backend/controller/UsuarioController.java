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
import unq.losrecursionistas.backend.services.interfaces.UsuarioService;

@RestController
@RequestMapping
public class UsuarioController {

	private final UsuarioService service;

	public UsuarioController(UsuarioService service) { this.service = service; }

	@PostMapping("/users")
	public ResponseEntity<AltaUsuarioResponse> crear(@Valid @RequestBody AltaUsuarioRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request.username()));
	}

	@PostMapping("/auth/token")
	public ResponseEntity<TokenResponse> token(@Valid @RequestBody TokenRequest request) {
		return ResponseEntity.ok(service.emitirToken(request.username(), request.apiKey()));
	}
}