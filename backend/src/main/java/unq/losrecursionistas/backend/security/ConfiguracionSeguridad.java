package unq.losrecursionistas.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import unq.losrecursionistas.backend.security.handlers.JwtAuthenticationEntryPoint;
import unq.losrecursionistas.backend.security.jwt.impl.JwtAuthFilter;
import unq.losrecursionistas.backend.service.interfaces.JwtService;
import org.springframework.security.core.userdetails.UserDetailsService;
@Configuration
public class ConfiguracionSeguridad {

	@Bean
	SecurityFilterChain cadenaSeguridad(HttpSecurity http, JwtService jwtService,
			UserDetailsService userDetailsService) throws Exception {
		http.csrf(csrf -> csrf.disable())
			.sessionManagement(sesiones -> sesiones.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(errores -> errores.authenticationEntryPoint(new JwtAuthenticationEntryPoint()))
			.addFilterBefore(new JwtAuthFilter(jwtService, userDetailsService), UsernamePasswordAuthenticationFilter.class)
			.authorizeHttpRequests(autorizacion -> autorizacion
				.requestMatchers("/login", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**", "/actuator/health")
				.permitAll()
				.anyRequest().authenticated());
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration configuracion) throws Exception {
		return configuracion.getAuthenticationManager();
	}
}