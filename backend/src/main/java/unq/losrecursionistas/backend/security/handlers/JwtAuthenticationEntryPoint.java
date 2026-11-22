package unq.losrecursionistas.backend.security.handlers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final EscritorRespuestaError escritor;

	public JwtAuthenticationEntryPoint(EscritorRespuestaError escritor) {
		this.escritor = escritor;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException excepcion) throws IOException {
		escritor.escribir(response, HttpStatus.UNAUTHORIZED, "La autenticacion es requerida");
	}
}
