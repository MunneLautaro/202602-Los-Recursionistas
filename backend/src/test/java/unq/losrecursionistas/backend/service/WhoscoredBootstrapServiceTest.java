package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.adapter.whoscored.ScrapingConfiguration;
import unq.losrecursionistas.backend.adapter.whoscored.ScrapingPort;
import unq.losrecursionistas.backend.adapter.whoscored.dto.EquipoWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.JugadorWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.LigaWhoscoredDto;
import unq.losrecursionistas.backend.model.EjecucionIngesta;
import unq.losrecursionistas.backend.model.Equipo;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.model.Liga;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEquipo;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioJugador;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioLiga;
import unq.losrecursionistas.backend.service.impl.MatchingWhoscoredService;
import unq.losrecursionistas.backend.service.impl.WhoscoredBootstrapService;
import unq.losrecursionistas.backend.service.interfaces.IngestaWhoscoredService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WhoscoredBootstrapServiceTest {

    @Test
    void vinculaLigasEquiposYJugadores() {
        ScrapingPort scraping = mock(ScrapingPort.class);
        ScrapingConfiguration config = new ScrapingConfiguration();
        RepositorioLiga ligas = mock(RepositorioLiga.class);
        RepositorioEquipo equipos = mock(RepositorioEquipo.class);
        RepositorioJugador jugadores = mock(RepositorioJugador.class);
        MatchingWhoscoredService matching = mock(MatchingWhoscoredService.class);
        IngestaWhoscoredService ingesta = mock(IngestaWhoscoredService.class);
        Liga liga = new Liga(1L, "Premier League", "PL");
        Equipo equipo = Equipo.builder().id(2L).nombre("Real Madrid").build();
        Jugador jugador = Jugador.builder().id(3L).build();
        EquipoWhoscoredDto team = new EquipoWhoscoredDto(20L, "Real Madrid", "/team", 1L);
        JugadorWhoscoredDto player = new JugadorWhoscoredDto(30L, "Jugador", null, null, null, 20L, "Real Madrid", List.of());
        when(equipos.obtenerEquiposDeLiga(1L)).thenReturn(List.of(equipo));
        when(scraping.descubrirEquipos(10L)).thenReturn(List.of(team));
        when(scraping.descubrirJugadores(20L)).thenReturn(List.of(player));
        when(matching.vincular(player)).thenReturn(new MatchingWhoscoredService.ResultadoMatching(
                MatchingWhoscoredService.Estado.VINCULADO, jugador, null));
        when(ligas.obtenerLigas()).thenReturn(List.of(liga));
        WhoscoredBootstrapService service = new WhoscoredBootstrapService(scraping, config, ligas, equipos,
                jugadores, matching, ingesta);

        service.vincularEquipos(liga, 10L);
        service.vincularJugadores(team);
        assertThat(service.encontrarLiga(new LigaWhoscoredDto(1L, "Premier League", null, null))).isSameAs(liga);
        assertThat(service.esLigaInicial(new LigaWhoscoredDto(1L, "Premier League", null, null))).isTrue();
        config.setLigasPermitidas(List.of(99L));
        assertThat(service.esLigaInicial(new LigaWhoscoredDto(1L, "Premier League", null, null))).isFalse();
        service.cargarDatosIniciales();
        verify(ingesta).iniciar(EjecucionIngesta.Alcance.COMPLETA, null);
    }
}
