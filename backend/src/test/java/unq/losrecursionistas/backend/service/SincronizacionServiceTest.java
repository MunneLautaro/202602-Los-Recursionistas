package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unq.losrecursionistas.backend.client.footballdata.FootballDataApiClient;
import unq.losrecursionistas.backend.client.footballdata.FootballDataMapper;
import unq.losrecursionistas.backend.client.footballdata.dto.FootballCompetitionTeamsResponse;
import unq.losrecursionistas.backend.client.footballdata.dto.FootballPlayerResponse;
import unq.losrecursionistas.backend.client.footballdata.dto.FootballTeamResponse;
import unq.losrecursionistas.backend.model.Equipo;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.model.Liga;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioLiga;
import unq.losrecursionistas.backend.service.impl.SincronizacionService;
import unq.losrecursionistas.backend.service.interfaces.EquipoService;
import unq.losrecursionistas.backend.service.interfaces.JugadorService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SincronizacionServiceTest {

    @Mock
    private FootballDataApiClient apiClient;

    @Mock
    private FootballDataMapper mapper;

    @Mock
    private JugadorService jugadorService;

    @Mock
    private EquipoService equipoService;

    @Mock
    private RepositorioLiga repositorioLiga;

    private SincronizacionService sincronizacionService;

    @BeforeEach
    void setUp() {
        sincronizacionService = new SincronizacionService(apiClient, mapper, jugadorService, equipoService, repositorioLiga);
    }

    @Test
    void sincronizarLigaExitosamente() {
        String codigoLiga = "PL";
        FootballCompetitionTeamsResponse.CompetitionDto compDto = new FootballCompetitionTeamsResponse.CompetitionDto(2021L, "Premier League", "PL");
        FootballTeamResponse teamDto = new FootballTeamResponse(64L, "Liverpool FC", "Liverpool", "LIV", "http://escudo.png", List.of());
        FootballCompetitionTeamsResponse response = new FootballCompetitionTeamsResponse(compDto, List.of(teamDto));

        Liga ligaMock = new Liga(2021L, "Premier League", "PL");
        Equipo equipoMock = Equipo.builder().id(1L).idExterno(64L).nombre("Liverpool FC").build();
        FootballPlayerResponse playerDto = new FootballPlayerResponse(10L, "Salah", "Mohamed", "Forward", "1992-06-15", "Egypt");
        FootballTeamResponse teamFullDto = new FootballTeamResponse(64L, "Liverpool FC", "Liverpool", "LIV", "http://escudo.png", List.of(playerDto));
        Jugador jugadorMock = Jugador.builder().id(100L).nombre("Salah").build();

        when(apiClient.getEquiposPorCompeticion(codigoLiga)).thenReturn(response);
        when(repositorioLiga.buscarOModificarPorCodigoOIdExterno(eq("PL"), eq(2021L), eq("Premier League"))).thenReturn(ligaMock);
        when(mapper.equipoAModelo(teamDto)).thenReturn(equipoMock);
        when(equipoService.guardarOActualizar(any(Equipo.class))).thenReturn(equipoMock);
        when(apiClient.getEquipo(64L)).thenReturn(teamFullDto);
        when(mapper.aModelo(anyList())).thenReturn(List.of(jugadorMock));

        sincronizacionService.sincronizarLiga(codigoLiga);

        verify(apiClient).getEquiposPorCompeticion(codigoLiga);
        verify(repositorioLiga).buscarOModificarPorCodigoOIdExterno("PL", 2021L, "Premier League");
        verify(equipoService).guardarOActualizar(any(Equipo.class));
        verify(apiClient).getEquipo(64L);
        verify(jugadorService).guardarTodos(anyList());
    }

    @Test
    void sincronizarTodasLasLigasManejaExcepcionesIndividuales() {
        when(apiClient.getEquiposPorCompeticion("PL")).thenThrow(new RuntimeException("API error"));

        sincronizacionService.sincronizarTodasLasLigas(List.of("PL", "BL1"));

        verify(apiClient).getEquiposPorCompeticion("PL");
        verify(apiClient).getEquiposPorCompeticion("BL1");
    }
}
