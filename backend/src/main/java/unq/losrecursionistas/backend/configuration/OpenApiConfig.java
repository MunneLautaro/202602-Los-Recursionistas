package unq.losrecursionistas.backend.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI mercadoOpenApi() {
		return new OpenAPI().info(new Info()
				.title("Mercado de Tokens de Jugadores API")
				.version("1.0.0")
				.description("API REST para catalogo y mercado simulado de tokens."));
	}
}