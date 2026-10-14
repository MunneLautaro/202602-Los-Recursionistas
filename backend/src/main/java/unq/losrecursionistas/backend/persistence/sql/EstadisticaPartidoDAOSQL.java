package unq.losrecursionistas.backend.persistence.sql;

import org.springframework.data.jpa.repository.JpaRepository;
import unq.losrecursionistas.backend.model.EstadisticaPartido;

import java.util.Optional;

public interface EstadisticaPartidoDAOSQL extends JpaRepository<EstadisticaPartido, Long> {
    Optional<EstadisticaPartido> findByJugadorIdAndPartidoWhoscoredId(Long jugadorId, Long partidoWhoscoredId);
}
