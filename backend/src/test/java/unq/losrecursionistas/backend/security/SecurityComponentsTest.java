package unq.losrecursionistas.backend.security;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import unq.losrecursionistas.backend.adapter.whoscored.ScrapingException;
import unq.losrecursionistas.backend.security.handlers.JwtAccessDeniedHandler;
import unq.losrecursionistas.backend.security.handlers.JwtAuthenticationEntryPoint;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityComponentsTest {

    @Test
    void escribeRespuestasJsonDeSeguridad() throws Exception {
        MockHttpServletResponse denied = new MockHttpServletResponse();
        new JwtAccessDeniedHandler().handle(new MockHttpServletRequest(), denied,
                new AccessDeniedException("denegado"));
        assertThat(denied.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        assertThat(denied.getContentAsString()).contains("No tiene permisos suficientes");

        MockHttpServletResponse unauthenticated = new MockHttpServletResponse();
        new JwtAuthenticationEntryPoint().commence(new MockHttpServletRequest(), unauthenticated,
                new AuthenticationException("no autenticado") {});
        assertThat(unauthenticated.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(unauthenticated.getContentAsString()).contains("unauthorized");
    }

    @Test
    void conservaDatosDeScrapingException() {
        Throwable cause = new IllegalStateException("fallo");
        ScrapingException exception = new ScrapingException("mensaje", "/url", "parseo", cause);

        assertThat(exception.getUrl()).isEqualTo("/url");
        assertThat(exception.getFase()).isEqualTo("parseo");
        assertThat(exception.getCause()).isSameAs(cause);
    }
}
