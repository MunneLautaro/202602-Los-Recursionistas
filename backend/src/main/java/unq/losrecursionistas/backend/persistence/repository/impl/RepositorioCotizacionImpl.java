package unq.losrecursionistas.backend.persistence.repository.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.model.Cotizacion;
import unq.losrecursionistas.backend.model.RankingCotizacion;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioCotizacion;
import unq.losrecursionistas.backend.persistence.sql.CotizacionDAOSQL;

import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class RepositorioCotizacionImpl implements RepositorioCotizacion {
    private static final LocalDateTime FECHA_MINIMA = LocalDateTime.of(1, 1, 1, 0, 0);
    private static final LocalDateTime FECHA_MAXIMA = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    private final CotizacionDAOSQL dao;

    public RepositorioCotizacionImpl(CotizacionDAOSQL dao) {
        this.dao = dao;
    }

    @Override
    public Cotizacion guardar(Cotizacion cotizacion) {
        return dao.save(cotizacion);
    }

    @Override
    public Cotizacion vigente(Long jugadorId) {
        return dao.findFirstByJugadorIdOrderByTimestampCalculoDesc(jugadorId).orElse(null);
    }

    @Override
    public Page<Cotizacion> buscarPorJugador(Long jugadorId, java.time.LocalDate desde,
                                             java.time.LocalDate hasta, Pageable pageable) {
        return dao.buscarPorJugador(jugadorId,
                desde == null ? FECHA_MINIMA : desde.atStartOfDay(),
                hasta == null ? FECHA_MAXIMA : hasta.plusDays(1).atStartOfDay(), pageable);
    }

    @Override
    public List<Cotizacion> buscarSeriePorJugador(Long jugadorId, java.time.LocalDate desde,
                                                  java.time.LocalDate hasta, int limite) {
        var recientes = new ArrayList<>(dao.buscarSeriePorJugador(jugadorId,
                desde == null ? FECHA_MINIMA : desde.atStartOfDay(),
                hasta == null ? FECHA_MAXIMA : hasta.plusDays(1).atStartOfDay(),
                PageRequest.of(0, limite)));
        Collections.reverse(recientes);
        return recientes;
    }

    @Override
    public Page<RankingCotizacion> buscarRanking(Long ligaId, String posicion, Pageable pageable) {
        return dao.buscarRanking(ligaId, posicion, pageable)
                .map(r -> new RankingCotizacion(r.getJugadorId(), r.getNombre(), r.getPosicion(),
                        r.getEquipo(), r.getLiga(), r.getValorActual(), r.getValorAnterior(),
                        r.getVariacion()));
    }
}
