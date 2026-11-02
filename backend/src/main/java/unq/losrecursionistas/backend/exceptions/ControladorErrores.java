package unq.losrecursionistas.backend.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.stream.Collectors;

@ControllerAdvice
public class ControladorErrores {

    private static final Logger log = LoggerFactory.getLogger(ControladorErrores.class);

	@ExceptionHandler(ExcepcionValidacion.class)
	public ResponseEntity<RespuestaError> manejarValidacion(ExcepcionValidacion excepcion) {
		return respuesta(HttpStatus.BAD_REQUEST, excepcion.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<RespuestaError> manejarArgumentoInvalido(MethodArgumentNotValidException excepcion) {
		String detalle = excepcion.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining("; "));
		if (detalle.isBlank()) {
			detalle = excepcion.getMessage();
		}
		return respuesta(HttpStatus.BAD_REQUEST, "La solicitud no supera las validaciones", detalle);
	}

	@ExceptionHandler(ExcepcionDominio.class)
	public ResponseEntity<RespuestaError> manejarDominio(ExcepcionDominio excepcion) {
		return respuesta(HttpStatus.UNPROCESSABLE_ENTITY, excepcion.getMessage());
	}

	@ExceptionHandler(ExcepcionRecursoNoEncontrado.class)
	public ResponseEntity<RespuestaError> manejarRecursoNoEncontrado(ExcepcionRecursoNoEncontrado excepcion) {
	    return respuesta(HttpStatus.NOT_FOUND, excepcion.getMessage());
	}

	@ExceptionHandler(ExcepcionCotizacionActiva.class)
	public ResponseEntity<RespuestaError> manejarCotizacionActiva(ExcepcionCotizacionActiva excepcion) {
	    return respuesta(HttpStatus.CONFLICT, excepcion.getMessage());
	}

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> manejarErrorNoControlado(Exception excepcion) {
        log.error("Error no controlado", excepcion);
        String mensaje = excepcion.getMessage() != null && !excepcion.getMessage().isBlank()
                ? excepcion.getMessage()
                : excepcion.getClass().getSimpleName();
        String detalle = obtenerCausaRaiz(excepcion);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, mensaje, detalle);
    }

	private String obtenerCausaRaiz(Throwable excepcion) {
		Throwable causa = excepcion;
		while (causa.getCause() != null && causa.getCause() != causa) {
			causa = causa.getCause();
		}
		return causa.getMessage() != null && !causa.getMessage().isBlank()
				? causa.getMessage()
				: causa.toString();
	}

	private ResponseEntity<RespuestaError> respuesta(HttpStatus estado, String mensaje) {
		return respuesta(estado, mensaje, estado.getReasonPhrase());
	}

	private ResponseEntity<RespuestaError> respuesta(HttpStatus estado, String mensaje, String detalle) {
		RespuestaError cuerpo = new RespuestaError(estado.value(), mensaje, detalle, Instant.now());
		return ResponseEntity.status(estado).body(cuerpo);
	}
}
