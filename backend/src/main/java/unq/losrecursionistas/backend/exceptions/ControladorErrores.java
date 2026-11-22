package unq.losrecursionistas.backend.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Clock;
import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Traduce cualquier excepción que escape de un controller a una {@link RespuestaError} con el
 * código HTTP adecuado.
 *
 * <ul>
 *   <li>Excepciones de dominio propias ({@code Excepcion*}).</li>
 *   <li>Excepciones estándar de Spring MVC (404 de ruta, 405, 415, parámetros faltantes o mal
 *       tipados, cuerpo ilegible, etc.), heredadas de {@link ResponseEntityExceptionHandler}.</li>
 *   <li>Seguridad: {@link AuthenticationException} → 401 y {@link AccessDeniedException} → 403
 *       (o 401 si no hay usuario autenticado).</li>
 *   <li>Validación, conflictos de persistencia y, como último recurso, 500 sin exponer detalles
 *       internos.</li>
 * </ul>
 */
@RestControllerAdvice
public class ControladorErrores extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ControladorErrores.class);
	private static final String MENSAJE_ERROR_INTERNO = "Ocurrio un error inesperado";

	// ---------- Excepciones de dominio ----------

	@ExceptionHandler(ExcepcionValidacion.class)
	public ResponseEntity<Object> manejarValidacion(ExcepcionValidacion excepcion) {
		return respuesta(HttpStatus.BAD_REQUEST, excepcion.getMessage());
	}

	@ExceptionHandler(ExcepcionDominio.class)
	public ResponseEntity<Object> manejarDominio(ExcepcionDominio excepcion) {
		return respuesta(HttpStatus.UNPROCESSABLE_CONTENT, excepcion.getMessage());
	}

	@ExceptionHandler(ExcepcionRecursoNoEncontrado.class)
	public ResponseEntity<Object> manejarRecursoNoEncontrado(ExcepcionRecursoNoEncontrado excepcion) {
		return respuesta(HttpStatus.NOT_FOUND, excepcion.getMessage());
	}

	@ExceptionHandler(ExcepcionCotizacionActiva.class)
	public ResponseEntity<Object> manejarCotizacionActiva(ExcepcionCotizacionActiva excepcion) {
		return respuesta(HttpStatus.CONFLICT, excepcion.getMessage());
	}

	// ---------- Seguridad ----------

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<Object> manejarAutenticacion(AuthenticationException excepcion) {
		return respuesta(HttpStatus.UNAUTHORIZED, "La autenticacion es requerida");
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Object> manejarAccesoDenegado(AccessDeniedException excepcion) {
		if (!hayUsuarioAutenticado()) {
			return respuesta(HttpStatus.UNAUTHORIZED, "La autenticacion es requerida");
		}
		return respuesta(HttpStatus.FORBIDDEN, mensajeOPorDefecto(excepcion, "No tiene permisos suficientes"));
	}

	// ---------- Validación y argumentos inválidos ----------

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Object> manejarRestricciones(ConstraintViolationException excepcion) {
		String detalle = excepcion.getConstraintViolations().stream()
				.map(ControladorErrores::describir)
				.collect(Collectors.joining("; "));
		return respuesta(HttpStatus.BAD_REQUEST, "La solicitud no supera las validaciones",
				detalle.isBlank() ? excepcion.getMessage() : detalle);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Object> manejarArgumentoIlegal(IllegalArgumentException excepcion) {
		return respuesta(HttpStatus.BAD_REQUEST, mensajeOPorDefecto(excepcion, "La solicitud es invalida"));
	}

	// ---------- Persistencia ----------

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Object> manejarIntegridadDeDatos(DataIntegrityViolationException excepcion) {
		log.warn("Violacion de integridad de datos", excepcion);
		return respuesta(HttpStatus.CONFLICT, "La operacion entra en conflicto con datos existentes");
	}

	@ExceptionHandler(ConcurrencyFailureException.class)
	public ResponseEntity<Object> manejarConcurrencia(ConcurrencyFailureException excepcion) {
		log.warn("Conflicto de concurrencia", excepcion);
		return respuesta(HttpStatus.CONFLICT,
				"El recurso fue modificado por otra operacion, intente nuevamente");
	}

	// ---------- Último recurso ----------

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> manejarErrorNoControlado(Exception excepcion) {
		log.error("Error no controlado", excepcion);
		return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, MENSAJE_ERROR_INTERNO);
	}

	// ---------- Excepciones estándar de Spring MVC ----------

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException excepcion,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		String detalle = excepcion.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining("; "));
		if (detalle.isBlank()) {
			detalle = excepcion.getBindingResult().getAllErrors().stream()
					.map(MessageSourceResolvable::getDefaultMessage)
					.collect(Collectors.joining("; "));
		}
		return respuesta(status, headers, "La solicitud no supera las validaciones", detalle);
	}

	@Override
	protected ResponseEntity<Object> handleHandlerMethodValidationException(
			HandlerMethodValidationException excepcion, HttpHeaders headers, HttpStatusCode status,
			WebRequest request) {
		String detalle = excepcion.getParameterValidationResults().stream()
				.flatMap(resultado -> resultado.getResolvableErrors().stream()
						.map(error -> resultado.getMethodParameter().getParameterName() + ": "
								+ error.getDefaultMessage()))
				.collect(Collectors.joining("; "));
		return respuesta(status, headers, "La solicitud no supera las validaciones", detalle);
	}

	/**
	 * Punto único por el que pasan todas las excepciones que resuelve
	 * {@link ResponseEntityExceptionHandler}: se reemplaza el ProblemDetail por {@link RespuestaError}
	 * para mantener un formato homogéneo, conservando los headers (por ejemplo {@code Allow} en 405).
	 */
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception excepcion, Object body,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		if (status.is5xxServerError()) {
			log.error("Error del servidor procesando la solicitud", excepcion);
			return respuesta(status, headers, MENSAJE_ERROR_INTERNO, null);
		}
		String mensaje = excepcion instanceof ErrorResponse errorResponse
				&& errorResponse.getBody().getDetail() != null
				? errorResponse.getBody().getDetail()
				: mensajeOPorDefecto(excepcion, null);
		return respuesta(status, headers, mensaje, null);
	}

	// ---------- Utilidades ----------

	private static boolean hayUsuarioAutenticado() {
		Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
		return autenticacion != null
				&& autenticacion.isAuthenticated()
				&& !(autenticacion instanceof AnonymousAuthenticationToken);
	}

	private static String describir(ConstraintViolation<?> violacion) {
		String ruta = violacion.getPropertyPath().toString();
		int ultimoPunto = ruta.lastIndexOf('.');
		String campo = ultimoPunto >= 0 ? ruta.substring(ultimoPunto + 1) : ruta;
		return campo + ": " + violacion.getMessage();
	}

	private static String mensajeOPorDefecto(Throwable excepcion, String porDefecto) {
		String mensaje = excepcion.getMessage();
		return mensaje != null && !mensaje.isBlank() ? mensaje : porDefecto;
	}

	private ResponseEntity<Object> respuesta(HttpStatusCode estado, String mensaje) {
		return respuesta(estado, null, mensaje, null);
	}

	private ResponseEntity<Object> respuesta(HttpStatusCode estado, String mensaje, String detalle) {
		return respuesta(estado, null, mensaje, detalle);
	}

	private ResponseEntity<Object> respuesta(HttpStatusCode estado, HttpHeaders headers, String mensaje,
			String detalle) {
		String frase = frase(estado);
		RespuestaError cuerpo = new RespuestaError(
				estado.value(),
				mensaje != null && !mensaje.isBlank() ? mensaje : frase,
				detalle != null && !detalle.isBlank() ? detalle : frase,
				Instant.now(Clock.systemUTC()));
		return ResponseEntity.status(estado).headers(headers).body(cuerpo);
	}

	private static String frase(HttpStatusCode estado) {
		HttpStatus conocido = HttpStatus.resolve(estado.value());
		return conocido != null ? conocido.getReasonPhrase() : String.valueOf(estado.value());
	}
}
