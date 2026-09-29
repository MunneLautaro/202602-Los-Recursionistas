package unq.losrecursionistas.backend.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionOpenApi {

	@Bean
	OpenAPI apiOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("API REST de Los Recursionistas")
						.version("1.0.0")
						.description("Contrato publico de la API REST del backend y sus verificaciones de salud."));
	}
}