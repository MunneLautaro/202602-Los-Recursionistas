package unq.losrecursionistas.backend.persistence.sql;

import org.springframework.data.jpa.repository.JpaRepository;
import unq.losrecursionistas.backend.model.EstadisticaPartido;

import java.util.Optional;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface EstadisticaPartidoDAOSQL extends JpaRepository<EstadisticaPartido, Long> {
    Optional<EstadisticaPartido> findByJugadorIdAndPartidoWhoscoredId(Long jugadorId, Long partidoWhoscoredId);
    List<EstadisticaPartido> findByJugadorIdAndTemporadaInicio(Long jugadorId, LocalDate temporadaInicio);
    List<EstadisticaPartido> findByJugadorIdAndFechaPartidoBetween(Long jugadorId, LocalDate desde, LocalDate hasta);
}
