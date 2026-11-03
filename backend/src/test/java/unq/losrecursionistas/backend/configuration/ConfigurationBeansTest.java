package unq.losrecursionistas.backend.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;
import unq.losrecursionistas.backend.adapter.footballdata.RestClientConfig;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationBeansTest {

    @Test
    void creaBeansDeConfiguracion() throws Exception {
        OpenAPI api = new ConfiguracionOpenApi().apiOpenApi();
        assertThat(api.getInfo().getTitle()).contains("Recursionistas");
        assertThat(new SchedulingConfig()).isNotNull();
        PasswordEncoder encoder = new SecurityConfig().passwordEncoder();
        assertThat(encoder.matches("clave", encoder.encode("clave"))).isTrue();

        RestClientConfig config = new RestClientConfig();
        set(config, "footballApiUrl", "https://example.test");
        set(config, "footballApiKey", "key");
        RestClient client = config.footballDataRestClient();
        assertThat(client).isNotNull();
    }

    private void set(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
