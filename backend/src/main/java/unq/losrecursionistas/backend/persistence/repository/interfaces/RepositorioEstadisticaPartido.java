package unq.losrecursionistas.backend.persistence.repository.interfaces;

import unq.losrecursionistas.backend.model.EstadisticaPartido;
import java.time.LocalDate;
import java.util.List;

public interface RepositorioEstadisticaPartido {
    EstadisticaPartido buscarPorJugadorYPartido(Long jugadorId, Long partidoWhoscoredId);

    EstadisticaPartido guardar(EstadisticaPartido estadistica);

    EstadisticaPartido upsert(EstadisticaPartido estadistica);
    List<EstadisticaPartido> buscarPorJugadorYTemporada(Long jugadorId, LocalDate temporadaInicio);
}
