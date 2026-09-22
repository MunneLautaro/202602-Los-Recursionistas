package unq.losrecursionistas.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import unq.losrecursionistas.backend.security.handlers.JwtAccessDeniedHandler;
import unq.losrecursionistas.backend.security.handlers.JwtAuthenticationEntryPoint;

class JwtHandlersTest {

	@Test
	void entryPointDevuelveJson401SinExponerDatosSensibles() throws Exception {
		var request = new MockHttpServletRequest();
		var response = new MockHttpServletResponse();
		var handler = new JwtAuthenticationEntryPoint();

		handler.commence(request, response, new AuthenticationException("bad credentials") {
		});

		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(response.getContentType()).contains(MediaType.APPLICATION_JSON_VALUE);
		assertThat(response.getContentAsString())
				.contains("\"error\":\"unauthorized\"")
				.contains("\"message\":\"La autenticacion es requerida\"");
		assertThat(response.getContentAsString())
				.doesNotContain("token")
				.doesNotContain("secret")
				.doesNotContain("contrasena")
				.doesNotContain("stack");
	}

	@Test
	void accessDeniedDevuelveJson403Diferenciado() throws Exception {
		var request = new MockHttpServletRequest();
		var response = new MockHttpServletResponse();
		var handler = new JwtAccessDeniedHandler();

		handler.handle(request, response, new AccessDeniedException("No tiene permisos suficientes"));

		assertThat(response.getStatus()).isEqualTo(403);
		assertThat(response.getContentType()).contains(MediaType.APPLICATION_JSON_VALUE);
		assertThat(response.getContentAsString())
				.contains("\"error\":\"forbidden\"")
				.contains("\"message\":\"No tiene permisos suficientes\"");
		assertThat(response.getContentAsString())
				.doesNotContain("token")
				.doesNotContain("secret")
				.doesNotContain("contrasena")
				.doesNotContain("stack");
	}
}
