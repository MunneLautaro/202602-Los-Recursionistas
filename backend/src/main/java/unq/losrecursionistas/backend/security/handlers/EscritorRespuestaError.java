package unq.losrecursionistas.backend.security.handlers;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import unq.losrecursionistas.backend.exceptions.RespuestaError;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;


public class EscritorRespuestaError {

	private final ObjectMapper objectMapper;

	public EscritorRespuestaError(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public void escribir(HttpServletResponse response, HttpStatus estado, String mensaje) throws IOException {
		RespuestaError cuerpo = new RespuestaError(estado.value(), mensaje, estado.getReasonPhrase(),
				Instant.now(Clock.systemUTC()));
		response.setStatus(estado.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().write(objectMapper.writeValueAsString(cuerpo));
	}
}
