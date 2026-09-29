package unq.losrecursionistas.backend.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class ControladorErrores {

    private static final Logger log = LoggerFactory.getLogger(ControladorErrores.class);

	@ExceptionHandler(ExcepcionValidacion.class)
	public ResponseEntity<RespuestaError> manejarValidacion(ExcepcionValidacion excepcion) {
		return respuesta(HttpStatus.BAD_REQUEST, excepcion.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<RespuestaError> manejarArgumentoInvalido(MethodArgumentNotValidException excepcion) {
		return respuesta(HttpStatus.BAD_REQUEST, "La solicitud no supera las validaciones");
	}

	@ExceptionHandler(ExcepcionDominio.class)
	public ResponseEntity<RespuestaError> manejarDominio(ExcepcionDominio excepcion) {
		return respuesta(HttpStatus.UNPROCESSABLE_ENTITY, excepcion.getMessage());
	}

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> manejarErrorNoControlado(Exception excepcion) {
        log.error("Error no controlado", excepcion);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno");
    }

	private ResponseEntity<RespuestaError> respuesta(HttpStatus estado, String mensaje) {
		RespuestaError cuerpo = new RespuestaError(estado.value(), mensaje, estado.getReasonPhrase(), Instant.now());
		return ResponseEntity.status(estado).body(cuerpo);
	}
}