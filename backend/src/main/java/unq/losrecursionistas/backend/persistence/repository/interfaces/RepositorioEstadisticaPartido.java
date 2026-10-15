package unq.losrecursionistas.backend.persistence.repository.interfaces;

import unq.losrecursionistas.backend.model.EstadisticaPartido;

public interface RepositorioEstadisticaPartido {
    EstadisticaPartido buscarPorJugadorYPartido(Long jugadorId, Long partidoWhoscoredId);

    EstadisticaPartido guardar(EstadisticaPartido estadistica);

    EstadisticaPartido upsert(EstadisticaPartido estadistica);
}
