package unq.losrecursionistas.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import unq.losrecursionistas.backend.controller.dto.CredencialesLoginDto;
import unq.losrecursionistas.backend.controller.dto.RespuestaTokenDto;
import unq.losrecursionistas.backend.controller.dto.usuario.UsuarioRequestDTO;
import unq.losrecursionistas.backend.controller.dto.usuario.UsuarioResponseDTO;
import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.service.interfaces.AuthService;

import java.util.Map;

@RestController
@RequestMapping
public class AutenticacionController {

    private final AuthService authService;

    public AutenticacionController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("registrar")
    @Operation(summary = "Registrar un nuevo usuario", description = "Crea una cuenta de usuario en el sistema")
    @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o nombre de usuario ya existente")
    public ResponseEntity<UsuarioResponseDTO> registrarUsuario(@Valid @RequestBody UsuarioRequestDTO usuarioRequestDTO) {
        Usuario nuevoUsuario = authService.registrarUsuario(usuarioRequestDTO.aModeloRegister());

        UsuarioResponseDTO responseDTO = UsuarioResponseDTO.desdeModelo(nuevoUsuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @Operation(summary = "Iniciar sesion y obtener JWT", description = "Autentica a un usuario registrado y devuelve un token JWT valido por una hora.")
    @ApiResponse(responseCode = "200", description = "Credenciales validas", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RespuestaTokenDto.class)))
    @ApiResponse(responseCode = "401", description = "Credenciales invalidas o incompletas", content = @Content(mediaType = "application/json"))
    @PostMapping("/login")
    public ResponseEntity<RespuestaTokenDto> login(@Valid @RequestBody CredencialesLoginDto credenciales) {
        return ResponseEntity.ok(authService.login(credenciales));
    }

    @ExceptionHandler({ AuthenticationException.class, IllegalArgumentException.class,
            MethodArgumentNotValidException.class, HttpMessageNotReadableException.class })
    public ResponseEntity<Map<String, String>> credencialesInvalidas() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "unauthorized", "message", "Credenciales invalidas"));
    }
}