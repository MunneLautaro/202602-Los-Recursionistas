package unq.losrecursionistas.backend.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${football.api.url}")
    private String footballApiUrl;

    @Value("${football.api.key}")
    private String footballApiKey;

    @Bean
    public RestClient footballDataRestClient() {
        return RestClient.builder()
                .baseUrl(footballApiUrl)
                .defaultHeader("X-Auth-Token", footballApiKey)
                .build();
    }
}
