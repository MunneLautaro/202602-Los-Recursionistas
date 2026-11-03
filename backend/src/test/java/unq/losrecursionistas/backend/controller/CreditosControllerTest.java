package unq.losrecursionistas.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.User;
import unq.losrecursionistas.backend.controller.dto.usuario.RecargaCreditosRequest;
import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioUsuario;
import unq.losrecursionistas.backend.service.interfaces.MercadoService;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CreditosControllerTest {

    @Test
    void recargaCreditosParaUsuarioAutenticado() {
        MercadoService mercado = mock(MercadoService.class);
        RepositorioUsuario usuarios = mock(RepositorioUsuario.class);
        Usuario solicitante = Usuario.builder().id(1L).nombreUsuario("ana").saldo(10D).build();
        Usuario actualizado = Usuario.builder().id(2L).nombreUsuario("beto").saldo(25D).build();
        when(usuarios.buscarPorNombreUsuario("ana")).thenReturn(solicitante);
        when(mercado.recargarCreditos(solicitante, 2L, BigDecimal.TEN)).thenReturn(actualizado);

        var response = new CreditosController(mercado, usuarios).recargar(
                2L, new RecargaCreditosRequest(BigDecimal.TEN), User.withUsername("ana").password("x").roles("USER").build());

        assertThat(response.getBody().saldo()).isEqualByComparingTo("25");
    }

    @Test
    void rechazaOperacionSinPrincipal() {
        MercadoService mercado = mock(MercadoService.class);
        RepositorioUsuario usuarios = mock(RepositorioUsuario.class);
        CreditosController controller = new CreditosController(mercado, usuarios);
        var request = new RecargaCreditosRequest(BigDecimal.ONE);

        assertThatThrownBy(() -> controller.saldo(1L, null)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> controller.recargar(1L, request, null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void permiteSaldoPropioYSaldoDeAdmin() {
        RepositorioUsuario usuarios = mock(RepositorioUsuario.class);
        Usuario propio = Usuario.builder().id(1L).nombreUsuario("ana").saldo(10D)
                .autoridades(Set.of("ROLE_USUARIO")).build();
        Usuario admin = Usuario.builder().id(9L).nombreUsuario("admin").saldo(10D)
                .autoridades(Set.of("ROLE_ADMIN")).build();
        when(usuarios.buscarPorNombreUsuario("ana")).thenReturn(propio);
        when(usuarios.buscarPorId(1L)).thenReturn(propio);
        when(usuarios.buscarPorId(9L)).thenReturn(admin);
        MercadoService mercado = mock(MercadoService.class);
        CreditosController controller = new CreditosController(mercado, usuarios);

        assertThat(controller.saldo(1L, User.withUsername("ana").password("x").roles("USER").build()).getBody().saldo())
                .isEqualByComparingTo("10");
        when(usuarios.buscarPorNombreUsuario("ana")).thenReturn(admin);
        assertThat(controller.saldo(9L, User.withUsername("ana").password("x").roles("USER").build()).getBody().saldo())
                .isEqualByComparingTo("10");
    }

    @Test
    void rechazaConsultarOtraCuenta() {
        RepositorioUsuario usuarios = mock(RepositorioUsuario.class);
        Usuario propio = Usuario.builder().id(1L).nombreUsuario("ana").saldo(10D)
                .autoridades(Set.of("ROLE_USUARIO")).build();
        when(usuarios.buscarPorNombreUsuario("ana")).thenReturn(propio);
        MercadoService mercado = mock(MercadoService.class);
        CreditosController controller = new CreditosController(mercado, usuarios);
        var principal = User.withUsername("ana").password("x").roles("USER").build();

        assertThatThrownBy(() -> controller.saldo(2L, principal))
                .isInstanceOf(AccessDeniedException.class);
    }
}
