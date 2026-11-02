package unq.losrecursionistas.backend.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableScheduling
@EnableAsync
public class SchedulingConfig {
}
