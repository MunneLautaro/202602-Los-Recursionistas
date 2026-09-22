package unq.losrecursionistas.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.MethodArgumentNotValidException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import unq.losrecursionistas.backend.controller.dto.CredencialesLoginDto;
import unq.losrecursionistas.backend.controller.dto.RespuestaTokenDto;
import unq.losrecursionistas.backend.service.interfaces.JwtService;

@RestController
@RequestMapping
public class AutenticacionController {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	public AutenticacionController(AuthenticationManager authenticationManager, JwtService jwtService) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
	}

	@Operation(summary = "Iniciar sesion y obtener JWT", description = "Autentica a un usuario registrado y devuelve un token JWT valido por una hora.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Credenciales validas", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RespuestaTokenDto.class))),
		@ApiResponse(responseCode = "401", description = "Credenciales invalidas o incompletas", content = @Content(mediaType = "application/json"))
	})
	@PostMapping("/login")
	public ResponseEntity<RespuestaTokenDto> login(@Valid @RequestBody CredencialesLoginDto credenciales) {
		var autenticacion = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
				credenciales.nombreUsuario(), credenciales.contrasena()));
		return ResponseEntity.ok(new RespuestaTokenDto(jwtService.generarToken((org.springframework.security.core.userdetails.UserDetails) autenticacion.getPrincipal())));
	}

	@ExceptionHandler({ AuthenticationException.class, IllegalArgumentException.class,
			MethodArgumentNotValidException.class, HttpMessageNotReadableException.class })
	public ResponseEntity<Map<String, String>> credencialesInvalidas() {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("error", "unauthorized", "message", "Credenciales invalidas"));
	}
}