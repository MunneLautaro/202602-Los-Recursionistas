package unq.losrecursionistas.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import unq.losrecursionistas.backend.security.handlers.EscritorRespuestaError;
import unq.losrecursionistas.backend.security.handlers.JwtAccessDeniedHandler;
import unq.losrecursionistas.backend.security.handlers.JwtAuthenticationEntryPoint;

import static org.assertj.core.api.Assertions.assertThat;

class HandlersSeguridadTest {

    private final JsonMapper mapper = JsonMapper.builder().build();
    private final EscritorRespuestaError escritor = new EscritorRespuestaError(mapper);

    @Test
    void entryPointResponde401ConFormatoRespuestaError() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new JwtAuthenticationEntryPoint(escritor).commence(new MockHttpServletRequest(), response,
                new InsufficientAuthenticationException("sin token"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith("application/json");
        JsonNode cuerpo = mapper.readTree(response.getContentAsString());
        assertThat(cuerpo.get("codigo").asInt()).isEqualTo(401);
        assertThat(cuerpo.get("mensaje").asString()).isEqualTo("La autenticacion es requerida");
        assertThat(cuerpo.get("detalle").asString()).isEqualTo("Unauthorized");
        assertThat(cuerpo.has("timestamp")).isTrue();
    }

    @Test
    void accessDeniedHandlerResponde403ConFormatoRespuestaError() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new JwtAccessDeniedHandler(escritor).handle(new MockHttpServletRequest(), response,
                new AccessDeniedException("prohibido"));

        assertThat(response.getStatus()).isEqualTo(403);
        JsonNode cuerpo = mapper.readTree(response.getContentAsString());
        assertThat(cuerpo.get("codigo").asInt()).isEqualTo(403);
        assertThat(cuerpo.get("mensaje").asString()).isEqualTo("No tiene permisos suficientes");
    }
}
