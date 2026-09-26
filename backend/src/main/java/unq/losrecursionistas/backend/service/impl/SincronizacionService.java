package unq.losrecursionistas.backend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unq.losrecursionistas.backend.client.footballdata.FootballDataApiClient;
import unq.losrecursionistas.backend.client.footballdata.FootballDataMapper;
import unq.losrecursionistas.backend.client.footballdata.dto.FootballTeamResponse;
import unq.losrecursionistas.backend.model.Equipo;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.service.interfaces.EquipoService;
import unq.losrecursionistas.backend.service.interfaces.JugadorService;

import java.util.List;

@Service
@Transactional
public class SincronizacionService {

    private final FootballDataApiClient apiClient;
    private final FootballDataMapper mapper;
    private final JugadorService jugadorService;
    private final EquipoService equipoService;

    public SincronizacionService(FootballDataApiClient apiClient, FootballDataMapper mapper, JugadorService jugadorService, EquipoService equipoService) {
        this.apiClient = apiClient;
        this.mapper = mapper;
        this.jugadorService = jugadorService;
        this.equipoService = equipoService;
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