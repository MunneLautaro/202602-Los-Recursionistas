package unq.losrecursionistas.backend.controller.dto;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.service.interfaces.ResultadoLogin;

import static org.assertj.core.api.Assertions.assertThat;

class RespuestaTokenDtoTest {

    @Test
    void desdeResultadoConUsuarioExponeSusDatos() {
        Usuario usuario = new Usuario("usuarioTest", "hash", 250.0);

        RespuestaTokenDto respuesta = RespuestaTokenDto.desde(
                new ResultadoLogin("jwt.token", "usuarioTest", usuario));

        assertThat(respuesta.token()).isEqualTo("jwt.token");
        assertThat(respuesta.usuario().nombreUsuario()).isEqualTo("usuarioTest");
        assertThat(respuesta.usuario().saldo()).isEqualTo(250.0);
    }

    @Test
    void desdeResultadoSinUsuarioPersistidoSoloExponeElNombre() {
        RespuestaTokenDto respuesta = RespuestaTokenDto.desde(
                new ResultadoLogin("jwt.token", "admin", null));

        assertThat(respuesta.token()).isEqualTo("jwt.token");
        assertThat(respuesta.usuario().id()).isNull();
        assertThat(respuesta.usuario().nombreUsuario()).isEqualTo("admin");
        assertThat(respuesta.usuario().autoridades()).isEmpty();
    }
}
