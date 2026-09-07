package unq.losrecursionistas.backend.exceptions;

import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import unq.losrecursionistas.backend.configuration.CorrelationIdFilter;
import unq.losrecursionistas.backend.exceptions.businessException.BusinessException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiError> handleDomain(BusinessException exception, HttpServletRequest request) {
		HttpStatus status = switch (exception.getCodigo()) {
			case "RECURSO_NO_ENCONTRADO" -> HttpStatus.NOT_FOUND;
			case "CONFLICTO" -> HttpStatus.CONFLICT;
			case "CREDENCIALES_INVALIDAS" -> HttpStatus.UNAUTHORIZED;
			default -> HttpStatus.BAD_REQUEST;
		};
		return ResponseEntity.status(status).body(response(exception.getCodigo(), exception.getMessage(), request, List.of()));
	}

	@ExceptionHandler({UsuarioYaExisteException.class, UsernameInvalidoException.class,
			ParametrosPaginacionInvalidosException.class})
	public ResponseEntity<ApiError> handleBadRequest(RuntimeException exception, HttpServletRequest request) {
		String code = exception instanceof UsuarioYaExisteException ? "CONFLICTO" : "VALIDACION_INVALIDA";
		HttpStatus status = exception instanceof UsuarioYaExisteException ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
		return ResponseEntity.status(status).body(response(code, exception.getMessage(), request, List.of()));
	}

	@ExceptionHandler(CredencialesInvalidasException.class)
	public ResponseEntity<ApiError> handleCredentials(CredencialesInvalidasException exception,
			HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(response("CREDENCIALES_INVALIDAS", exception.getMessage(), request, List.of()));
	}

	@ExceptionHandler(JugadorNoEncontradoException.class)
	public ResponseEntity<ApiError> handlePlayerNotFound(JugadorNoEncontradoException exception,
			HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(response("RECURSO_NO_ENCONTRADO", exception.getMessage(), request, List.of()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		List<String> details = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + Optional.ofNullable(error.getDefaultMessage()).orElse("valor invalido"))
				.toList();
		return ResponseEntity.badRequest().body(response("VALIDACION_INVALIDA", "La solicitud no es valida", request, details));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
		return ResponseEntity.internalServerError().body(response("ERROR_INTERNO", "Ocurrio un error interno", request, List.of()));
	}

	private ApiError response(String code, String message, HttpServletRequest request, List<String> details) {
		return new ApiError(code, message, request.getHeader(CorrelationIdFilter.HEADER), details);
	}
}