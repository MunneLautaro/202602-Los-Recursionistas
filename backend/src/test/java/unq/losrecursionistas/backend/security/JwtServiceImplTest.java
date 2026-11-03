package unq.losrecursionistas.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import unq.losrecursionistas.backend.security.jwt.impl.JwtServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceImplTest {

    @Test
    void generaExtraeYValidaToken() {
        var service = new JwtServiceImpl("01234567890123456789012345678901");
        var user = User.withUsername("ana").password("x").roles("USER").build();
        String token = service.generarToken(user);

        assertThat(service.extraerUsername(token)).contains("ana");
        assertThat(service.tokenValido(token, user)).isTrue();
        assertThat(service.tokenValido(token, User.withUsername("otro").password("x").build())).isFalse();
    }

    @Test
    void rechazaTokensInvalidos() {
        var service = new JwtServiceImpl("01234567890123456789012345678901");
        var user = User.withUsername("ana").password("x").build();

        assertThat(service.extraerUsername("invalid")).isEmpty();
        assertThat(service.tokenValido("invalid", user)).isFalse();
    }
}
