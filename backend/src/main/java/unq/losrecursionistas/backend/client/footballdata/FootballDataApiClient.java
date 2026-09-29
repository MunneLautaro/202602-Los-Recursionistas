package unq.losrecursionistas.backend.client.footballdata;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import unq.losrecursionistas.backend.client.footballdata.dto.FootballCompetitionTeamsResponse;
import unq.losrecursionistas.backend.client.footballdata.dto.FootballTeamResponse;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;
import unq.losrecursionistas.backend.model.Jugador;

import java.time.Duration;
import java.util.List;

@Component
public class FootballDataApiClient {

    private final RestClient restClient;
    private final FootballDataMapper mapper;
    private final RateLimiter rateLimiter;

    public FootballDataApiClient(RestClient restClient, FootballDataMapper mapper) {
        this.restClient = restClient;
        this.mapper = mapper;

        // Configuración de rate limiter: 10 llamadas por minuto (plan free football-data.org)
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(10)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ofMinutes(3))
                .build();
        this.rateLimiter = RateLimiter.of("footballDataApi", config);
    }

    public FootballCompetitionTeamsResponse getEquiposPorCompeticion(String codigoCompeticion) {
        return RateLimiter.decorateSupplier(rateLimiter, () -> {
            try {
                return restClient.get()
                        .uri("/competitions/{code}/teams", codigoCompeticion)
                        .retrieve()
                        .body(FootballCompetitionTeamsResponse.class);
            } catch (HttpClientErrorException.NotFound e) {
                throw new ExcepcionValidacion("No se encontro la competicion con codigo " + codigoCompeticion);
            } catch (RestClientResponseException e) {
                throw new ExcepcionValidacion("Error al comunicarse con la API de Football Data para competicion " + codigoCompeticion + ": " + e.getStatusCode());
            } catch (RestClientException e) {
                throw new ExcepcionValidacion("Error de conexion con la API de Football Data para competicion " + codigoCompeticion);
            }
        }).get();
    }

    public FootballTeamResponse getEquipo(Long equipoExternoId) {
        return RateLimiter.decorateSupplier(rateLimiter, () -> {
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
        }).get();
    }

    public List<Jugador> getJugadoresDeEquipo(Long equipoExternoId) {
        FootballTeamResponse equipo = getEquipo(equipoExternoId);

        if (equipo == null || equipo.squad() == null) {
            return List.of();
        }

        return mapper.aModelo(equipo.squad());
    }
}