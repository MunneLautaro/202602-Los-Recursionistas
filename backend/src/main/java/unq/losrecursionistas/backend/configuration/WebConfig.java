package unq.losrecursionistas.backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import unq.losrecursionistas.backend.configuration.CorrelationIdFilter;

@Configuration
public class WebConfig {

	@Bean
	CorrelationIdFilter correlationIdFilter() {
		return new CorrelationIdFilter();
	}
}