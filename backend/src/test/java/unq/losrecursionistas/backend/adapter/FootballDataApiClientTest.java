package unq.losrecursionistas.backend.adapter;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.http.HttpStatus;
import unq.losrecursionistas.backend.adapter.footballdata.FootballDataApiClient;
import unq.losrecursionistas.backend.adapter.footballdata.FootballDataMapper;
import unq.losrecursionistas.backend.adapter.footballdata.dto.FootballCompetitionTeamsResponse;
import unq.losrecursionistas.backend.adapter.footballdata.dto.FootballPlayerResponse;
import unq.losrecursionistas.backend.adapter.footballdata.dto.FootballTeamResponse;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FootballDataApiClientTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    @Mock
    private FootballDataMapper mapper;

    @Test
    void obtieneEquiposPorCompeticion() {
        FootballCompetitionTeamsResponse expected = new FootballCompetitionTeamsResponse(null, List.of());
        when(restClient.get().uri(anyString(), any(Object[].class)).retrieve().body(FootballCompetitionTeamsResponse.class))
                .thenReturn(expected);

        assertThat(new FootballDataApiClient(restClient, mapper).getEquiposPorCompeticion("PL"))
                .isSameAs(expected);
    }

    @Test
    void transformaJugadoresDelEquipo() {
        FootballTeamResponse team = new FootballTeamResponse(1L, "Equipo", "EQ", "EQ", "crest",
                List.of(new FootballPlayerResponse(2L, "Jugador", null, "MID", null, "ARG")));
        when(restClient.get().uri(anyString(), any(Object[].class)).retrieve().body(FootballTeamResponse.class))
                .thenReturn(team);
        when(mapper.aModelo(team.squad())).thenReturn(List.of());

        assertThat(new FootballDataApiClient(restClient, mapper).getJugadoresDeEquipo(1L)).isEmpty();
    }

    @Test
    void devuelveListaVaciaSinPlantel() {
        when(restClient.get().uri(anyString(), any(Object[].class)).retrieve().body(FootballTeamResponse.class))
                .thenReturn(null);

        assertThat(new FootballDataApiClient(restClient, mapper).getJugadoresDeEquipo(1L)).isEmpty();
        verifyNoInteractions(mapper);
    }

    @Test
    void convierteErroresDeCompeticionYEquipo() {
        FootballDataApiClient client = new FootballDataApiClient(restClient, mapper);
        when(restClient.get().uri(anyString(), any(Object[].class)).retrieve().body(FootballCompetitionTeamsResponse.class))
                .thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "not found", null, null, null));
        assertThatThrownBy(() -> client.getEquiposPorCompeticion("XX"))
                .isInstanceOf(ExcepcionValidacion.class);

        reset(restClient);
        when(restClient.get().uri(anyString(), any(Object[].class)).retrieve().body(FootballTeamResponse.class))
                .thenThrow(HttpClientErrorException.NotFound.create(HttpStatus.NOT_FOUND, "not found", null, null, null));
        assertThatThrownBy(() -> client.getEquipo(99L))
                .isInstanceOf(ExcepcionValidacion.class);
    }

    @Test
    void convierteErroresDeRespuestaYConexion() {
        FootballDataApiClient client = new FootballDataApiClient(restClient, mapper);
        when(restClient.get().uri(anyString(), any(Object[].class)).retrieve().body(FootballCompetitionTeamsResponse.class))
                .thenThrow(new RestClientResponseException("error", HttpStatus.BAD_GATEWAY.value(),
                        "bad gateway", null, null, null));
        assertThatThrownBy(() -> client.getEquiposPorCompeticion("PL"))
                .isInstanceOf(ExcepcionValidacion.class);

        reset(restClient);
        when(restClient.get().uri(anyString(), any(Object[].class)).retrieve().body(FootballCompetitionTeamsResponse.class))
                .thenThrow(new RestClientException("conexion"));
        assertThatThrownBy(() -> client.getEquiposPorCompeticion("PL"))
                .isInstanceOf(ExcepcionValidacion.class);

        reset(restClient);
        when(restClient.get().uri(anyString(), any(Object[].class)).retrieve().body(FootballTeamResponse.class))
                .thenThrow(new RestClientResponseException("error", HttpStatus.BAD_GATEWAY.value(),
                        "bad gateway", null, null, null));
        assertThatThrownBy(() -> client.getEquipo(1L))
                .isInstanceOf(ExcepcionValidacion.class);

        reset(restClient);
        when(restClient.get().uri(anyString(), any(Object[].class)).retrieve().body(FootballTeamResponse.class))
                .thenThrow(new RestClientException("conexion"));
        assertThatThrownBy(() -> client.getEquipo(1L))
                .isInstanceOf(ExcepcionValidacion.class);
    }
}
