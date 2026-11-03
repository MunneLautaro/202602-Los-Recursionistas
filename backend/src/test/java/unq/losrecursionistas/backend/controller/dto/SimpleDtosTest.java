package unq.losrecursionistas.backend.controller.dto;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.adapter.footballdata.FootballDataMapper;
import unq.losrecursionistas.backend.adapter.footballdata.dto.FootballPlayerResponse;
import unq.losrecursionistas.backend.adapter.footballdata.dto.FootballTeamResponse;
import unq.losrecursionistas.backend.controller.dto.cotizacion.RankingJugadorResponse;
import unq.losrecursionistas.backend.controller.dto.equipo.EquipoResponseDTO;
import unq.losrecursionistas.backend.controller.dto.jugador.JugadorFiltroRequestDTO;
import unq.losrecursionistas.backend.controller.dto.jugador.JugadorResponseDTO;
import unq.losrecursionistas.backend.controller.dto.liga.LigaResponseDTO;
import unq.losrecursionistas.backend.controller.dto.orden.OrdenRequest;
import unq.losrecursionistas.backend.controller.dto.orden.TenenciaTokenResponse;
import unq.losrecursionistas.backend.controller.dto.usuario.UsuarioRequestDTO;
import unq.losrecursionistas.backend.controller.dto.usuario.UsuarioResponseDTO;
import unq.losrecursionistas.backend.controller.dto.whoscored.IniciarIngestaRequest;
import unq.losrecursionistas.backend.controller.dto.whoscored.UltimaEjecucionResponse;
import unq.losrecursionistas.backend.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleDtosTest {

    @Test
    void convierteModelosBasicos() {
        Equipo equipo = Equipo.builder().id(1L).idExterno(2L).nombre("Equipo").nombreCorto("EQ")
                .sigla("E").escudoUrl("url").fundacion(1900).colores("rojo").estadio("Estadio").build();
        Jugador jugador = Jugador.builder().id(3L).idExterno(4L).nombre("Jugador").posicion("DEL")
                .nacionalidad("ARG").fechaNacimiento(LocalDate.of(2000, 1, 1)).equipo(equipo).build();
        Liga liga = Liga.builder().id(5L).idExterno(6L).nombre("Liga").codigo("L")
                .fechaCreacion(LocalDate.of(2020, 1, 1)).build();
        Usuario usuario = Usuario.builder().id(7L).nombreUsuario("ana").saldo(10D)
                .fechaCreacion(java.time.LocalDateTime.now()).build();

        assertThat(EquipoResponseDTO.desdeModelo(equipo).nombre()).isEqualTo("Equipo");
        assertThat(EquipoResponseDTO.desdeModelo(null)).isNull();
        assertThat(JugadorResponseDTO.desdeModelo(jugador).equipo().nombre()).isEqualTo("Equipo");
        assertThat(LigaResponseDTO.desdeModelo(liga).codigo()).isEqualTo("L");
        assertThat(UsuarioResponseDTO.desdeModelo(usuario).nombreUsuario()).isEqualTo("ana");
        assertThat(new UsuarioRequestDTO("ana", "x", 10D, false).aModelo().isHabilitado()).isFalse();
        assertThat(new UsuarioRequestDTO("ana", "x", 10D, false).aModeloRegister().isHabilitado()).isTrue();
        assertThat(new JugadorFiltroRequestDTO("ana", "DEL", 1L, null).aModelo()).isNotNull();
    }

    @Test
    void convierteRecordsDeIngestaYOrdenes() {
        EjecucionIngesta ejecucion = EjecucionIngesta.builder().id(1L)
                .estado(EjecucionIngesta.Estado.FINALIZADA).alcance(EjecucionIngesta.Alcance.COMPLETA)
                .inicio(OffsetDateTime.now()).build();
        assertThat(UltimaEjecucionResponse.from(null).disponible()).isFalse();
        assertThat(UltimaEjecucionResponse.from(ejecucion).disponible()).isTrue();
        assertThat(new IniciarIngestaRequest(EjecucionIngesta.Alcance.COMPLETA, null).alcance())
                .isEqualTo(EjecucionIngesta.Alcance.COMPLETA);
        assertThat(new RankingJugadorResponse(1L, "Jugador", "DEL", "Equipo", "Liga",
                BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ONE).jugadorId())
                .isEqualTo(1L);
        assertThat(new OrdenRequest(1L, 2).cantidad()).isEqualTo(2);
        assertThat(LoginUsuarioResponse.desde(Usuario.builder().id(1L).nombreUsuario("ana").saldo(1D)
                .fechaCreacion(java.time.LocalDateTime.now()).build()).nombreUsuario()).isEqualTo("ana");
        assertThat(new TenenciaTokenResponse(1L, 2L, "J", "E", 10, 2, 8).cantidadDisponible()).isEqualTo(8);
    }

    @Test
    void mapeaJugadoresDeFootballData() {
        FootballDataMapper mapper = new FootballDataMapper();
        FootballPlayerResponse player = new FootballPlayerResponse(1L, "Jugador", "Apellido",
                "MID", "2000-01-02", "ARG");
        assertThat(mapper.aModelo(player).getFechaNacimiento()).isEqualTo(LocalDate.of(2000, 1, 2));
        assertThat(mapper.aModelo(List.of(player))).hasSize(1);
        assertThat(mapper.equipoAModelo(new FootballTeamResponse(2L, "Equipo", "EQ", "E", "url", List.of()))
                .getNombre()).isEqualTo("Equipo");
    }
}
