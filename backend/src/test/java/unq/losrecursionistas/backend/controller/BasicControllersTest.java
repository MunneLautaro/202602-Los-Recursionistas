package unq.losrecursionistas.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import unq.losrecursionistas.backend.controller.dto.CredencialesLoginDto;
import unq.losrecursionistas.backend.controller.dto.RespuestaTokenDto;
import unq.losrecursionistas.backend.controller.dto.usuario.UsuarioRequestDTO;
import unq.losrecursionistas.backend.model.*;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEstadisticaTemporada;
import unq.losrecursionistas.backend.service.impl.SincronizacionService;
import unq.losrecursionistas.backend.service.interfaces.AuthService;
import unq.losrecursionistas.backend.service.interfaces.EquipoService;
import unq.losrecursionistas.backend.service.interfaces.JugadorService;
import unq.losrecursionistas.backend.service.interfaces.LigaService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BasicControllersTest {

    @Test
    void ejecutaAutenticacion() {
        AuthService auth = mock(AuthService.class);
        Usuario usuario = Usuario.builder().id(1L).nombreUsuario("ana").saldo(10D)
                .fechaCreacion(java.time.LocalDateTime.now()).build();
        when(auth.registrarUsuario(any())).thenReturn(usuario);
        RespuestaTokenDto token = new RespuestaTokenDto("jwt", null);
        when(auth.login(any())).thenReturn(token);
        AutenticacionController controller = new AutenticacionController(auth);

        assertThat(controller.registrarUsuario(new UsuarioRequestDTO("ana", "x", 10D, true))
                .getStatusCode().value()).isEqualTo(201);
        assertThat(controller.login(new CredencialesLoginDto("ana", "x")).getBody()).isSameAs(token);
        assertThat(controller.credencialesInvalidas().getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void listaEquiposYLigasYSincronizaJugadores() {
        EquipoService equipos = mock(EquipoService.class);
        LigaService ligas = mock(LigaService.class);
        Equipo equipo = Equipo.builder().id(1L).nombre("Equipo").build();
        Liga liga = new Liga(1L, "Liga", "L");
        when(equipos.obtenerTodosLosEquiposDeLiga(3L)).thenReturn(List.of(equipo));
        when(ligas.obtenerLigas()).thenReturn(List.of(liga));

        assertThat(new EquipoController(equipos).obtenerEquiposPorLiga(3L).getBody()).hasSize(1);
        assertThat(new LigaController(ligas).obtenerLigas().getBody()).hasSize(1);

        SincronizacionService sincronizacion = mock(SincronizacionService.class);
        assertThat(new SincronizacionController(sincronizacion).sincronizarJugadores(4L)
                .getStatusCode().value()).isEqualTo(202);
        verify(sincronizacion).sincronizarJugadores(4L);
    }

    @Test
    void consultaJugadorYEstadisticasConYSinRatings() {
        JugadorService jugadores = mock(JugadorService.class);
        RepositorioEstadisticaTemporada estadisticas = mock(RepositorioEstadisticaTemporada.class);
        var config = new unq.losrecursionistas.backend.adapter.whoscored.ScrapingConfiguration();
        config.setTemporadaInicio(LocalDate.of(2025, 1, 1));
        Equipo equipo = Equipo.builder().nombre("Equipo").build();
        Jugador jugador = Jugador.builder().id(1L).nombre("Jugador").posicion("DEL").equipo(equipo).build();
        EstadisticaTemporada stat = EstadisticaTemporada.builder().jugador(jugador).apps(10).goles(2).asistencias(null)
                .ratingPromedio(new BigDecimal("7.50")).build();
        when(jugadores.obtenerJugadorPorId(1L)).thenReturn(jugador);
        when(estadisticas.buscarPorJugadorYTemporada(eq(1L), any())).thenReturn(List.of(stat));
        var controller = new JugadorController(jugadores, estadisticas, config);

        assertThat(controller.obtenerJugadorPorId(1L).nombre()).isEqualTo("Jugador");
        assertThat(controller.estadisticas(1L).get("equipo")).isEqualTo("Equipo");
        assertThat(((java.util.Map<?, ?>) controller.estadisticas(1L).get("totalesGlobal"))
                .get("ratingPromedio")).isEqualTo(new BigDecimal("7.50"));
        when(jugadores.buscarJugadoresConFiltro(any(), eq(0))).thenReturn(Page.empty());
        assertThat(controller.buscarJugadoresConFiltro(
                new unq.losrecursionistas.backend.controller.dto.jugador.JugadorFiltroRequestDTO(null, null, null, null), 0))
                .isEmpty();
    }
}
