package unq.losrecursionistas.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:documentation-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ConfiguracionDocumentacionTest.HealthTestConfiguration.class)
class ConfiguracionDocumentacionTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TestHealthIndicator testHealthIndicator;

	@Test
	void lasRutasDeDocumentacionYHealthSonPublicas() throws Exception {
		mockMvc.perform(get("/swagger-ui.html"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/swagger-ui/index.html"));
		mockMvc.perform(get("/swagger-ui/index.html"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("application/json"));
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("UP")));
	}

	@Test
	void laEspecificacionOpenApiExponeMetadatosYHealth() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.openapi", Matchers.startsWith("3.")))
				.andExpect(jsonPath("$.info.title").value("API REST de Los Recursionistas"))
				.andExpect(jsonPath("$.info.version").value("1.0.0"))
				.andExpect(jsonPath("$.info.description").exists())
				.andExpect(jsonPath("$.paths['/actuator/health']").exists());
	}

	@Test
	void healthNoExponeDetallesSensiblesYActuatorSoloPublicaHealth() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.components").doesNotExist())
				.andExpect(jsonPath("$.details").doesNotExist());
		mockMvc.perform(get("/actuator/env"))
				.andExpect(resultado -> org.assertj.core.api.Assertions.assertThat(resultado.getResponse().getStatus()).isNotEqualTo(200));
		mockMvc.perform(get("/actuator/metrics"))
				.andExpect(resultado -> org.assertj.core.api.Assertions.assertThat(resultado.getResponse().getStatus()).isNotEqualTo(200));
	}

	@Test
	void healthDownResponde503() throws Exception {
		testHealthIndicator.setUnhealthy(true);
		try {
			mockMvc.perform(get("/actuator/health"))
					.andExpect(status().isServiceUnavailable())
					.andExpect(jsonPath("$.status").value("DOWN"));
		} finally {
			testHealthIndicator.setUnhealthy(false);
		}
	}

	@Test
	void laEspecificacionNoExponeSecretosNiAutenticacionBearer() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.not(Matchers.containsStringIgnoringCase("password"))))
				.andExpect(content().string(Matchers.not(Matchers.containsStringIgnoringCase("credentials"))))
				.andExpect(content().string(Matchers.not(Matchers.containsStringIgnoringCase("bearer"))))
				.andExpect(content().string(Matchers.not(Matchers.containsStringIgnoringCase("authorization"))));
	}

	@TestConfiguration
	static class HealthTestConfiguration {

		@Bean
		TestHealthIndicator testHealthIndicator() {
			return new TestHealthIndicator();
		}
	}

	static class TestHealthIndicator implements HealthIndicator {

		private volatile boolean unhealthy;

		@Override
		public Health health() {
			return unhealthy ? Health.down().build() : Health.up().build();
		}

		void setUnhealthy(boolean unhealthy) {
			this.unhealthy = unhealthy;
		}
	}
}