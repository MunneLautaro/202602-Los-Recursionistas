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

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(DomainException.class)
	public ResponseEntity<ErrorResponse> handleDomain(DomainException exception, HttpServletRequest request) {
		HttpStatus status = switch (exception.getCodigo()) {
			case "RECURSO_NO_ENCONTRADO" -> HttpStatus.NOT_FOUND;
			case "CONFLICTO" -> HttpStatus.CONFLICT;
			case "CREDENCIALES_INVALIDAS" -> HttpStatus.UNAUTHORIZED;
			default -> HttpStatus.BAD_REQUEST;
		};
		return ResponseEntity.status(status).body(response(exception.getCodigo(), exception.getMessage(), request, List.of()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		List<String> details = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + Optional.ofNullable(error.getDefaultMessage()).orElse("valor invalido"))
				.toList();
		return ResponseEntity.badRequest().body(response("VALIDACION_INVALIDA", "La solicitud no es valida", request, details));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
		return ResponseEntity.internalServerError().body(response("ERROR_INTERNO", "Ocurrio un error interno", request, List.of()));
	}

	private ErrorResponse response(String code, String message, HttpServletRequest request, List<String> details) {
		return new ErrorResponse(code, message, request.getHeader(CorrelationIdFilter.HEADER), details);
	}
}