package unq.losrecursionistas.backend.security.handlers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class JwtAccessDeniedHandler implements AccessDeniedHandler {

	private final EscritorRespuestaError escritor;

	public JwtAccessDeniedHandler(EscritorRespuestaError escritor) {
		this.escritor = escritor;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		escritor.escribir(response, HttpStatus.FORBIDDEN, "No tiene permisos suficientes");
	}
}
