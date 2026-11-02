package unq.losrecursionistas.backend.persistence.repository.impl;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import unq.losrecursionistas.backend.model.EstadisticaTemporada;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEstadisticaTemporada;
import unq.losrecursionistas.backend.persistence.sql.EstadisticaTemporadaDAOSQL;

import java.time.LocalDate;
import java.util.List;

@Component
public class RepositorioEstadisticaTemporadaImpl implements RepositorioEstadisticaTemporada {
    private final EstadisticaTemporadaDAOSQL dao;

    public RepositorioEstadisticaTemporadaImpl(EstadisticaTemporadaDAOSQL dao) {
        this.dao = dao;
    }

    @Override
    public List<EstadisticaTemporada> buscarPorJugadorYTemporada(Long jugadorId, LocalDate temporadaInicio) {
        return dao.findByJugadorIdAndTemporadaInicio(jugadorId, temporadaInicio);
    }

    @Override
    public List<EstadisticaTemporada> buscarRanking(String liga, String posicion, LocalDate temporadaInicio, int limit) {
        return dao.buscarRanking(liga, posicion, temporadaInicio).stream().limit(limit).toList();
    }

    @Override
    @Transactional
    public void reemplazarPorJugadorYTemporada(Long jugadorId, LocalDate temporadaInicio,
                                                 List<EstadisticaTemporada> estadisticas) {
        dao.deleteAll(dao.findByJugadorIdAndTemporadaInicio(jugadorId, temporadaInicio));
        dao.saveAll(estadisticas);
    }
}
