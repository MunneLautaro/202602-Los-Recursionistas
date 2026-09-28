package unq.losrecursionistas.backend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unq.losrecursionistas.backend.client.footballdata.FootballDataApiClient;
import unq.losrecursionistas.backend.client.footballdata.FootballDataMapper;
import unq.losrecursionistas.backend.client.footballdata.dto.FootballCompetitionTeamsResponse;
import unq.losrecursionistas.backend.client.footballdata.dto.FootballTeamResponse;
import unq.losrecursionistas.backend.model.Equipo;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.model.Liga;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioLiga;
import unq.losrecursionistas.backend.service.interfaces.EquipoService;
import unq.losrecursionistas.backend.service.interfaces.JugadorService;

import java.util.List;

@Service
@Transactional
public class SincronizacionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SincronizacionService.class);

    private final FootballDataApiClient apiClient;
    private final FootballDataMapper mapper;
    private final JugadorService jugadorService;
    private final EquipoService equipoService;
    private final RepositorioLiga repositorioLiga;

    public SincronizacionService(FootballDataApiClient apiClient,
                                 FootballDataMapper mapper,
                                 JugadorService jugadorService,
                                 EquipoService equipoService,
                                 RepositorioLiga repositorioLiga) {
        this.apiClient = apiClient;
        this.mapper = mapper;
        this.jugadorService = jugadorService;
        this.equipoService = equipoService;
        this.repositorioLiga = repositorioLiga;
    }

    public void sincronizarTodasLasLigas(List<String> codigosLigas) {
        for (String codigoLiga : codigosLigas) {
            try {
                sincronizarLiga(codigoLiga);
            } catch (Exception e) {
                LOGGER.error("Error al sincronizar la liga con código {}: {}", codigoLiga, e.getMessage(), e);
            }
        }
    }

    public void sincronizarLiga(String codigoLiga) {
        LOGGER.info("Iniciando sincronización para la liga con código: {}", codigoLiga);
        FootballCompetitionTeamsResponse response = apiClient.getEquiposPorCompeticion(codigoLiga);

        if (response == null || response.teams() == null) {
            LOGGER.warn("No se obtuvieron equipos para la competición: {}", codigoLiga);
            return;
        }

        Long ligaIdExterno = response.competition() != null ? response.competition().id() : null;
        String nombreLiga = response.competition() != null ? response.competition().name() : codigoLiga;

        Liga liga = repositorioLiga.buscarOModificarPorCodigoOIdExterno(codigoLiga, ligaIdExterno, nombreLiga);

        for (FootballTeamResponse teamSummaryDto : response.teams()) {
            try {
                sincronizarEquipoYPlantel(teamSummaryDto, liga);
            } catch (Exception e) {
                LOGGER.error("Error al sincronizar el equipo ID {} ({}): {}", teamSummaryDto.id(), teamSummaryDto.name(), e.getMessage(), e);
            }
        }
        LOGGER.info("Sincronización completada para la liga: {}", codigoLiga);
    }

    private void sincronizarEquipoYPlantel(FootballTeamResponse teamSummaryDto, Liga liga) {
        Equipo equipoModel = mapper.equipoAModelo(teamSummaryDto);
        equipoModel.setLiga(liga);
        Equipo equipoGuardado = equipoService.guardarOActualizar(equipoModel);

        FootballTeamResponse teamFullDto = apiClient.getEquipo(teamSummaryDto.id());
        if (teamFullDto != null && teamFullDto.squad() != null) {
            List<Jugador> jugadores = mapper.aModelo(teamFullDto.squad());
            for (Jugador jugador : jugadores) {
                jugador.setEquipo(equipoGuardado);
            }
            jugadorService.guardarTodos(jugadores);
        }
    }

    public void sincronizarJugadores(Long equipoExternoId) {
        FootballTeamResponse teamDto = apiClient.getEquipo(equipoExternoId);
        if (teamDto == null) {
            return;
        }

        Equipo equipo = equipoService.buscarPorIdExterno(equipoExternoId, mapper.equipoAModelo(teamDto));

        List<Jugador> jugadores = mapper.aModelo(teamDto.squad() != null ? teamDto.squad() : List.of());
        for (Jugador jugador : jugadores) {
            jugador.setEquipo(equipo);
        }

        jugadorService.guardarTodos(jugadores);
    }
}