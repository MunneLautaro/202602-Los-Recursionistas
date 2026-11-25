package unq.losrecursionistas.backend.observabilidad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import unq.losrecursionistas.backend.service.interfaces.JwtService;
import unq.losrecursionistas.backend.service.interfaces.UsuarioDetailsService;

import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:observabilidad;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
		"scheduler.whoscored.enabled=false",
		"scheduler.cotizacion.enabled=false",
		"scheduler.football-data.startup-enabled=false",
		"whoscored.scraping.startup-enabled=false",
		"management.endpoints.web.exposure.include=health,info,metrics,prometheus"
})
@AutoConfigureMockMvc
class ObservabilidadSeguridadTest {

	private static final String RUTA_HEALTH = "/actuator/health";
	private static final String RUTA_NO_APROBADA = "/actuator/env";
	private static final String TOKEN_ADMIN = "token-admin";
	private static final String TOKEN_USUARIO = "token-usuario";
	private static final String NOMBRE_ADMIN = "admin";
	private static final String NOMBRE_USUARIO = "usuario";

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private UsuarioDetailsService usuarioDetailsService;

	@BeforeEach
	void configurarTokens() {
		when(jwtService.extraerUsername(TOKEN_ADMIN)).thenReturn(Optional.of(NOMBRE_ADMIN));
		when(jwtService.extraerUsername(TOKEN_USUARIO)).thenReturn(Optional.of(NOMBRE_USUARIO));
		when(jwtService.tokenValido(anyString(), any())).thenReturn(true);
		when(usuarioDetailsService.loadUserByUsername(NOMBRE_ADMIN))
				.thenReturn(User.withUsername(NOMBRE_ADMIN).password("x").authorities("ROLE_ADMIN").build());
		when(usuarioDetailsService.loadUserByUsername(NOMBRE_USUARIO))
				.thenReturn(User.withUsername(NOMBRE_USUARIO).password("x").authorities("ROLE_USUARIO").build());
	}

	@Test
	void testHealthAnonimoRespondeEstadoGlobalSinComponentes() throws Exception {
		mvc.perform(get(RUTA_HEALTH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").exists())
				.andExpect(jsonPath("$.components").doesNotExist());
	}

	@ParameterizedTest
	@MethodSource("rutasProtegidas")
	void testEndpointProtegidoSinTokenRespondeNoAutorizado(String ruta) throws Exception {
		mvc.perform(get(ruta))
				.andExpect(status().isUnauthorized());
	}

	@ParameterizedTest
	@MethodSource("rutasProtegidas")
	void testEndpointProtegidoConRolUsuarioRespondeProhibido(String ruta) throws Exception {
		mvc.perform(get(ruta).header(HttpHeaders.AUTHORIZATION, bearer(TOKEN_USUARIO)))
				.andExpect(status().isForbidden());
	}

	@ParameterizedTest
	@MethodSource("rutasProtegidas")
	void testEndpointProtegidoConRolAdminRespondeOk(String ruta) throws Exception {
		mvc.perform(get(ruta).header(HttpHeaders.AUTHORIZATION, bearer(TOKEN_ADMIN)))
				.andExpect(status().isOk());
	}

	@Test
	void testEndpointNoAprobadoConRolAdminRespondeNoEncontrado() throws Exception {
		mvc.perform(get(RUTA_NO_APROBADA).header(HttpHeaders.AUTHORIZATION, bearer(TOKEN_ADMIN)))
				.andExpect(status().isNotFound());
	}

	static Stream<String> rutasProtegidas() {
		return Stream.of("/actuator/info", "/actuator/metrics", "/actuator/prometheus");
	}

	private static String bearer(String token) {
		return "Bearer " + token;
	}
}
