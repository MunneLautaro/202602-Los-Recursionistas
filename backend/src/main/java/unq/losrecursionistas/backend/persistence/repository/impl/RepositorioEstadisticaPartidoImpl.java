package unq.losrecursionistas.backend.persistence.repository.impl;

import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.model.EstadisticaPartido;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEstadisticaPartido;
import unq.losrecursionistas.backend.persistence.sql.EstadisticaPartidoDAOSQL;

@Component
public class RepositorioEstadisticaPartidoImpl implements RepositorioEstadisticaPartido {
    private final EstadisticaPartidoDAOSQL dao;

    public RepositorioEstadisticaPartidoImpl(EstadisticaPartidoDAOSQL dao) {
        this.dao = dao;
    }

    @Override
    public EstadisticaPartido buscarPorJugadorYPartido(Long jugadorId, Long partidoWhoscoredId) {
        return dao.findByJugadorIdAndPartidoWhoscoredId(jugadorId, partidoWhoscoredId).orElse(null);
    }

    @Override
    public EstadisticaPartido guardar(EstadisticaPartido estadistica) {
        return dao.save(estadistica);
    }
}
