package unq.losrecursionistas.backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioUsuario;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:login-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginAutenticacionTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RepositorioUsuario repositorioUsuario;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void prepararUsuario() {
		repositorioUsuario.buscarPorNombreUsuario("jugador-login")
				.orElseGet(() -> repositorioUsuario.guardar(
						new Usuario("jugador-login", passwordEncoder.encode("secreto-login"), BigDecimal.ZERO)));
	}

	@Test
	void devuelveTokenParaCredencialesValidas() throws Exception {
		mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombreUsuario\":\"jugador-login\",\"contrasena\":\"secreto-login\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token").isString())
			.andExpect(jsonPath("$.token").isNotEmpty())
			.andExpect(jsonPath("$.contrasena").doesNotExist());
	}

	@Test
	void rechazaCredencialesInvalidasOIncompletasSinDetalles() throws Exception {
		mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombreUsuario\":\"jugador-login\",\"contrasena\":\"incorrecta\"}"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.token").doesNotExist())
			.andExpect(jsonPath("$.contrasena").doesNotExist());

		mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombreUsuario\":\"\"}"))
			.andExpect(status().isUnauthorized());
	}
}