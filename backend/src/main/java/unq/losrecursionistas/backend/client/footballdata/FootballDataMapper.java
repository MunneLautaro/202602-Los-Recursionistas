package unq.losrecursionistas.backend.client.footballdata;

import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.client.footballdata.dto.FootballPlayerResponse;
import unq.losrecursionistas.backend.client.footballdata.dto.FootballTeamResponse;
import unq.losrecursionistas.backend.model.Equipo;
import unq.losrecursionistas.backend.model.Jugador;

import java.time.LocalDate;
import java.util.List;

@Component
public class FootballDataMapper {

    public Jugador aModelo(FootballPlayerResponse dto) {
        return Jugador.builder()
                .idExterno(dto.id())
                .nombre(dto.name())
                .posicion(dto.position())
                .nacionalidad(dto.nationality())
                .fechaNacimiento(
                        dto.dateOfBirth() != null ? LocalDate.parse(dto.dateOfBirth()) : null
                )
                .activo(true)
                .build();
    }

    public List<Jugador> aModelo(List<FootballPlayerResponse> dtos) {
        return dtos.stream()
                .map(this::aModelo)
                .toList();
    }

    public Equipo equipoAModelo (FootballTeamResponse dto) {
        return Equipo.builder()
                .idExterno(dto.id())
                .nombre(dto.name())
                .nombreCorto(dto.shortName())
                .sigla(dto.tla())
                .escudoUrl(dto.crest())
                .build();
    }
}
