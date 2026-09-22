package unq.losrecursionistas.backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioUsuario;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:seguridad-rutas-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(RutasSeguridadTest.RutasPublicasTestConfiguration.class)
class RutasSeguridadTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RepositorioUsuario repositorioUsuario;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void prepararUsuarios() {
		repositorioUsuario.buscarPorNombreUsuario("usuario-publico")
				.orElseGet(() -> repositorioUsuario.guardar(
						new Usuario("usuario-publico", passwordEncoder.encode("secreto-publico"), BigDecimal.ZERO)));
		repositorioUsuario.buscarPorNombreUsuario("usuario-admin")
				.orElseGet(() -> repositorioUsuario.guardar(
						new Usuario("usuario-admin", passwordEncoder.encode("secreto-admin"), BigDecimal.ZERO)));
	}

	@Test
	void rutasPublicasQuedanAccesiblesSinToken() throws Exception {
		mockMvc.perform(get("/public/estado"))
				.andExpect(status().isOk())
				.andExpect(content().string("publico"));

		mockMvc.perform(get("/auth/health"))
				.andExpect(status().isOk())
				.andExpect(content().string("auth-publico"));
	}

	@Test
	void rutasPrivadasSinTokenDevuelven401() throws Exception {
		mockMvc.perform(get("/ruta-privada-test"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.error").value("unauthorized"));
	}

	@Test
	void identidadAutenticadaSinPermisoRecibe403() throws Exception {
		var autenticacion = new UsernamePasswordAuthenticationToken(
				"usuario-publico",
				null,
				List.of(new SimpleGrantedAuthority("ROLE_USUARIO")));
		SecurityContextHolder.getContext().setAuthentication(autenticacion);

		mockMvc.perform(get("/ruta-admin"))
				.andExpect(status().isForbidden())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.error").value("forbidden"))
				.andExpect(jsonPath("$.message").value("No tiene permisos suficientes"));
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class RutasPublicasTestConfiguration {

		@Bean
		PublicRouteController publicRouteController() {
			return new PublicRouteController();
		}
	}

	@RestController
	static class PublicRouteController {

		@GetMapping("/public/estado")
		String publico() {
			return "publico";
		}

		@GetMapping("/auth/health")
		String authPublico() {
			return "auth-publico";
		}

		@GetMapping("/ruta-privada-test")
		String privado() {
			return "privado";
		}

		@GetMapping("/ruta-admin")
		String admin() {
			return "admin";
		}
	}
}
