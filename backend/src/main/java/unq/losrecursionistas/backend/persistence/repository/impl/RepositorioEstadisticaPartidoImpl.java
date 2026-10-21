package unq.losrecursionistas.backend.persistence.repository.impl;

import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.model.EstadisticaPartido;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEstadisticaPartido;
import unq.losrecursionistas.backend.persistence.sql.EstadisticaPartidoDAOSQL;
import java.time.LocalDate;
import java.util.List;

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

    @Override
    public EstadisticaPartido upsert(EstadisticaPartido estadistica) {
        EstadisticaPartido existente = buscarPorJugadorYPartido(
                estadistica.getJugador().getId(), estadistica.getPartidoWhoscoredId());
        if (existente == null) {
            return dao.save(estadistica);
        }
        existente.setFechaPartido(estadistica.getFechaPartido());
        existente.setTorneo(estadistica.getTorneo());
        existente.setTorneoWhoscoredId(estadistica.getTorneoWhoscoredId());
        existente.setTemporadaInicio(estadistica.getTemporadaInicio());
        existente.setTemporadaFin(estadistica.getTemporadaFin());
        existente.setEquipoWhoscoredId(estadistica.getEquipoWhoscoredId());
        existente.setEquipoNombre(estadistica.getEquipoNombre());
        existente.setRivalWhoscoredId(estadistica.getRivalWhoscoredId());
        existente.setRivalNombre(estadistica.getRivalNombre());
        existente.setLocalVisitante(estadistica.getLocalVisitante());
        existente.setMinutos(estadistica.getMinutos());
        existente.setRating(estadistica.getRating());
        existente.setGoles(estadistica.getGoles());
        existente.setAsistencias(estadistica.getAsistencias());
        existente.setTiros(estadistica.getTiros());
        existente.setTirosAlArco(estadistica.getTirosAlArco());
        existente.setPases(estadistica.getPases());
        existente.setPasesClave(estadistica.getPasesClave());
        existente.setRegates(estadistica.getRegates());
        existente.setTackles(estadistica.getTackles());
        existente.setIntercepciones(estadistica.getIntercepciones());
        existente.setFaltasCometidas(estadistica.getFaltasCometidas());
        existente.setAmarillas(estadistica.getAmarillas());
        existente.setRojas(estadistica.getRojas());
        existente.setPrecisionPases(estadistica.getPrecisionPases());
        existente.setStatsCrudas(estadistica.getStatsCrudas());
        return dao.save(existente);
    }

    @Override
    public List<EstadisticaPartido> buscarPorJugadorYTemporada(Long jugadorId, LocalDate temporadaInicio) {
        return dao.findByJugadorIdAndTemporadaInicio(jugadorId, temporadaInicio);
    }
}
