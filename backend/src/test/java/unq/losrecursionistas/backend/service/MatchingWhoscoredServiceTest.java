package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.adapter.whoscored.dto.JugadorWhoscoredDto;
import unq.losrecursionistas.backend.model.Equipo;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioJugador;
import unq.losrecursionistas.backend.service.impl.MatchingWhoscoredService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MatchingWhoscoredServiceTest {
    private RepositorioJugador repositorio;
    private MatchingWhoscoredService service;

    @BeforeEach
    void setUp() {
        repositorio = mock(RepositorioJugador.class);
        service = new MatchingWhoscoredService(repositorio);
    }

    @Test
    void normalizaAcentosEspaciosYSlugCodificado() {
        assertThat(MatchingWhoscoredService.normalizar("  Juli%C3%A1n   Alvarez "))
                .isEqualTo("julian alvarez");
    }

    @Test
    void priorizaElIdWhoscoredYActualizaPosicion() {
        Jugador jugador = jugador("Lionel Messi", "Delantero", true);
        when(repositorio.buscarPorIdWhoscored(10L)).thenReturn(jugador);

        MatchingWhoscoredService.ResultadoMatching resultado = service.vincular(
                new JugadorWhoscoredDto(10L, "Otro nombre", "/player/10", null,
                        "Argentina", 20L, "Equipo", List.of("Mediapunta")));

        assertThat(resultado.estado()).isEqualTo(MatchingWhoscoredService.Estado.VINCULADO);
        assertThat(jugador.getPosicion()).isEqualTo("Mediapunta");
        verify(repositorio, never()).obtenerJugadoresActivos();
    }

    @Test
    void usaEquipoYNacionalidadParaDesempatar() {
        Equipo equipo = new Equipo(null, null, null, null, null, null, null);
        equipo.setIdWhoscored(20L);
        Jugador candidato = jugador("Julián Álvarez", "Delantero", true);
        candidato.setFechaNacimiento(LocalDate.of(2000, 1, 1));
        candidato.setNacionalidad("Argentina");
        candidato.setEquipo(equipo);
        when(repositorio.buscarPorIdWhoscored(11L)).thenReturn(null);
        when(repositorio.obtenerJugadoresActivos()).thenReturn(List.of(candidato));

        MatchingWhoscoredService.ResultadoMatching resultado = service.buscar(
                new JugadorWhoscoredDto(11L, "Julian Alvarez", "/player/11", candidato.getFechaNacimiento(),
                        "Argentina", 20L, "Equipo", List.of()));

        assertThat(resultado.estado()).isEqualTo(MatchingWhoscoredService.Estado.VINCULADO);
        assertThat(resultado.jugador()).isSameAs(candidato);
    }

    @Test
    void conservaLaAmbiguedadSinElegirUnJugador() {
        Jugador primero = jugador("Alex Garcia", "Defensor", true);
        Jugador segundo = jugador("Alex Garcia", "Defensor", true);
        when(repositorio.buscarPorIdWhoscored(12L)).thenReturn(null);
        when(repositorio.obtenerJugadoresActivos()).thenReturn(List.of(primero, segundo));

        MatchingWhoscoredService.ResultadoMatching resultado = service.buscar(
                new JugadorWhoscoredDto(12L, "Alex Garcia", "/player/12", null,
                        null, null, null, List.of()));

        assertThat(resultado.estado()).isEqualTo(MatchingWhoscoredService.Estado.AMBIGUO);
        assertThat(resultado.jugador()).isNull();
        assertThat(resultado.candidatos()).hasSize(2);
    }

    private Jugador jugador(String nombre, String posicion, boolean activo) {
        return new Jugador(nombre, posicion,
                new Equipo(null, null, null, null, null, null, null), activo);
    }
}
