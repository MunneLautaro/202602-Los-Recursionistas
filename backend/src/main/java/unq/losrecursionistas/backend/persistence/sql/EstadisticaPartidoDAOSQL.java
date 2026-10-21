package unq.losrecursionistas.backend.persistence.sql;

import org.springframework.data.jpa.repository.JpaRepository;
import unq.losrecursionistas.backend.model.EstadisticaPartido;

import java.util.Optional;
import java.time.LocalDate;
import java.util.List;

public interface EstadisticaPartidoDAOSQL extends JpaRepository<EstadisticaPartido, Long> {
    Optional<EstadisticaPartido> findByJugadorIdAndPartidoWhoscoredId(Long jugadorId, Long partidoWhoscoredId);
    List<EstadisticaPartido> findByJugadorIdAndTemporadaInicio(Long jugadorId, LocalDate temporadaInicio);
}
