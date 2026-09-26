package unq.losrecursionistas.backend.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import unq.losrecursionistas.backend.client.footballdata.FootballDataApiClient;
import unq.losrecursionistas.backend.client.footballdata.FootballDataMapper;
import unq.losrecursionistas.backend.client.footballdata.dto.FootballTeamResponse;
import unq.losrecursionistas.backend.model.Equipo;

@Service
@Transactional
public class FootballService {

    private final FootballDataApiClient apiClient;
    private final FootballDataMapper mapper;

    public FootballService(FootballDataApiClient apiClient, FootballDataMapper mapper) {
        this.apiClient = apiClient;
        this.mapper = mapper;
    }

}
