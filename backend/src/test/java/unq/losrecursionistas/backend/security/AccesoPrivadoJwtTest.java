package unq.losrecursionistas.backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioUsuario;
import unq.losrecursionistas.backend.service.interfaces.JwtService;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:private-jwt-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(AccesoPrivadoJwtTest.PrivateRouteConfiguration.class)
class AccesoPrivadoJwtTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RepositorioUsuario repositorioUsuario;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private PrivateRouteController privateRouteController;

	@BeforeEach
	void prepararUsuario() {
		repositorioUsuario.buscarPorNombreUsuario("jugador-privado")
				.orElseGet(() -> repositorioUsuario.guardar(new Usuario("jugador-privado", "hash", BigDecimal.ZERO)));
		privateRouteController.visitas.set(0);
	}

	@Test
	void rechazaSolicitudSinTokenYNoAlcanzaElEndpoint() throws Exception {
		mockMvc.perform(get("/ruta-privada-test"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.error").value("unauthorized"))
				.andExpect(jsonPath("$.message").isString());

		org.assertj.core.api.Assertions.assertThat(privateRouteController.visitas).hasValue(0);
	}

	@Test
	void permiteSolicitudConBearerValidoYCargaIdentidad() throws Exception {
		var detalles = userDetailsService.loadUserByUsername("jugador-privado");
		var token = jwtService.generarToken(detalles);

		mockMvc.perform(get("/ruta-privada-test").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().string("jugador-privado"));

		org.assertj.core.api.Assertions.assertThat(privateRouteController.visitas).hasValue(1);
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class PrivateRouteConfiguration {

		@Bean
		PrivateRouteController privateRouteController() {
			return new PrivateRouteController();
		}
	}

	@RestController
	static class PrivateRouteController {

		private final AtomicInteger visitas = new AtomicInteger();

		@GetMapping("/ruta-privada-test")
		String atender(org.springframework.security.core.Authentication autenticacion) {
			visitas.incrementAndGet();
			return autenticacion.getName();
		}
	}
}