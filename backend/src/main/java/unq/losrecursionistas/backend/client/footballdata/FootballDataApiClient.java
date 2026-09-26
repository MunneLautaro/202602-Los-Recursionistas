package unq.losrecursionistas.backend.client.footballdata;

import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import unq.losrecursionistas.backend.client.footballdata.dto.FootballTeamResponse;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;
import unq.losrecursionistas.backend.model.Jugador;

import java.util.List;

@Component
public class FootballDataApiClient {

    private final RestClient restClient;
    private final FootballDataMapper mapper;

    public FootballDataApiClient(RestClient restClient, FootballDataMapper mapper) {
        this.restClient = restClient;
        this.mapper = mapper;
    }

    public FootballTeamResponse getEquipo(Long equipoExternoId) {
        try {
            return restClient.get()
                    .uri("/teams/{id}", equipoExternoId)
                    .retrieve()
                    .body(FootballTeamResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ExcepcionValidacion("No se encontro el equipo externo con ID " + equipoExternoId);
        } catch (RestClientResponseException e) {
            throw new ExcepcionValidacion("Error al comunicarse con la API de Football Data: " + e.getStatusCode());
        } catch (RestClientException e) {
            throw new ExcepcionValidacion("Error de conexion con la API de Football Data");
        }
    }

    public List<Jugador> getJugadoresDeEquipo(Long equipoExternoId) {
        FootballTeamResponse equipo = getEquipo(equipoExternoId);

        if (equipo == null || equipo.squad() == null) {
            return List.of();
        }

        return mapper.aModelo(equipo.squad());
    }
}