package unq.losrecursionistas.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ConfiguracionSeguridad {

	@Bean
	SecurityFilterChain cadenaSeguridad(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(autorizacion -> autorizacion.anyRequest().permitAll());
		return http.build();
	}
}