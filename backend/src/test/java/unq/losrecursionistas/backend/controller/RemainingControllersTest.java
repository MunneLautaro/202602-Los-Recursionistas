package unq.losrecursionistas.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.User;
import unq.losrecursionistas.backend.adapter.whoscored.ScrapingConfiguration;
import unq.losrecursionistas.backend.controller.dto.orden.OrdenRequest;
import unq.losrecursionistas.backend.controller.dto.whoscored.IniciarIngestaRequest;
import unq.losrecursionistas.backend.model.*;
import unq.losrecursionistas.backend.persistence.repository.interfaces.*;
import unq.losrecursionistas.backend.service.impl.WhoscoredBootstrapService;
import unq.losrecursionistas.backend.service.interfaces.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RemainingControllersTest {

    @Test
    void delegaIngestaYSincronizacion() {
        IngestaWhoscoredService ingesta = mock(IngestaWhoscoredService.class);
        ResultadoIngesta resultado = new ResultadoIngesta(1, 2, 3, 4);
        when(ingesta.ejecutarLinking()).thenReturn(resultado);
        when(ingesta.ejecutarIngesta()).thenReturn(resultado);
        when(ingesta.ingestarEstadisticasJugador(7L)).thenReturn(resultado);
        IngestaController controller = new IngestaController(ingesta);

        assertThat(controller.linking()).isSameAs(resultado);
        assertThat(controller.estadisticas()).isSameAs(resultado);
        assertThat(controller.estadisticasJugador(7L)).isSameAs(resultado);
        verify(ingesta).ingestarEstadisticasJugador(7L);
    }

    @Test
    void protegeYDelegaOperacionesDeMercado() {
        MercadoService mercado = mock(MercadoService.class);
        RepositorioUsuario usuarios = mock(RepositorioUsuario.class);
        Usuario usuario = Usuario.builder().id(1L).nombreUsuario("ana").saldo(100D)
                .autoridades(Set.of("ROLE_USUARIO")).build();
        Jugador jugador = Jugador.builder().id(2L).nombre("Jugador").posicion("DEL")
                .equipo(Equipo.builder().id(3L).nombre("Equipo").build()).build();
        Orden orden = Orden.builder().id(4L).jugador(jugador).tipo(TipoOrden.COMPRA)
                .cantidadOriginal(2).cantidadDisponible(2).precioUnitario(BigDecimal.TEN)
                .estado(EstadoOrden.ACTIVA).build();
        when(usuarios.buscarPorNombreUsuario("ana")).thenReturn(usuario);
        when(mercado.crearOrden(eq(usuario), eq(2L), eq(2), any())).thenReturn(orden);
        MercadoController controller = new MercadoController(mercado, usuarios);
        var principal = User.withUsername("ana").password("x").roles("USER").build();
        OrdenRequest request = new OrdenRequest(2L, 2);

        assertThat(controller.comprar(request, principal).getBody().id()).isEqualTo(4L);
        assertThat(controller.vender(request, principal).getBody().id()).isEqualTo(4L);
        assertThatThrownBy(() -> controller.comprar(request, null)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void listaOrdenesTransaccionesYTenencias() {
        MercadoService mercado = mock(MercadoService.class);
        RepositorioUsuario usuarios = mock(RepositorioUsuario.class);
        Usuario usuario = Usuario.builder().id(1L).nombreUsuario("ana").saldo(100D)
                .autoridades(Set.of("ROLE_USUARIO")).build();
        when(usuarios.buscarPorNombreUsuario("ana")).thenReturn(usuario);
        when(mercado.listarOrdenes(any(), any(), any(), any())).thenReturn(Page.empty());
        when(mercado.listarTransacciones(anyLong(), any())).thenReturn(Page.empty());
        when(mercado.listarOrdenesDeUsuario(anyLong(), any())).thenReturn(Page.empty());
        when(mercado.listarTenencias(anyLong(), any())).thenReturn(Page.empty());
        MercadoController controller = new MercadoController(mercado, usuarios);
        var principal = User.withUsername("ana").password("x").roles("USER").build();

        assertThat(controller.listar(null, null, null, 0, 0)).isEmpty();
        assertThat(controller.transacciones(1L, 0, 0, principal)).isEmpty();
        assertThat(controller.ordenesDelUsuario(1L, 0, 0, principal)).isEmpty();
        assertThat(controller.tenencias(1L, 0, 0, principal)).isEmpty();
        assertThatThrownBy(() -> controller.transacciones(2L, 0, 1, principal))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void obtienePerfilYRechazaPrincipalInvalido() {
        RepositorioUsuario usuarios = mock(RepositorioUsuario.class);
        Usuario usuario = Usuario.builder().id(1L).nombreUsuario("ana").saldo(10D).build();
        when(usuarios.buscarPorNombreUsuario("ana")).thenReturn(usuario);
        UsuarioController controller = new UsuarioController(usuarios);

        assertThat(controller.obtenerPerfil(User.withUsername("ana").password("x").build()).getBody().id())
                .isEqualTo(1L);
        assertThatThrownBy(() -> controller.obtenerPerfil(null)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> controller.obtenerPerfil(User.withUsername(" ").password("x").build()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void iniciaYConsultaIngestaWhoscored() {
        IngestaWhoscoredService ingesta = mock(IngestaWhoscoredService.class);
        EjecucionIngestaService ejecuciones = mock(EjecucionIngestaService.class);
        EjecucionIngesta ejecucion = EjecucionIngesta.builder().id(8L)
                .estado(EjecucionIngesta.Estado.ACTIVA).alcance(EjecucionIngesta.Alcance.COMPLETA)
                .inicio(java.time.OffsetDateTime.now()).build();
        when(ingesta.iniciar(EjecucionIngesta.Alcance.COMPLETA, null)).thenReturn(ejecucion);
        when(ejecuciones.ultima()).thenReturn(ejecucion);
        WhoscoredIngestaController controller = new WhoscoredIngestaController(ingesta, ejecuciones);

        assertThat(controller.iniciar(new IniciarIngestaRequest(EjecucionIngesta.Alcance.COMPLETA, null))
                .getStatusCode().value()).isEqualTo(202);
        assertThat(controller.ultima().disponible()).isTrue();
    }
}
