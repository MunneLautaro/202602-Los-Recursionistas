package unq.losrecursionistas.backend.persistence.repository.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.model.Cotizacion;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioCotizacion;
import unq.losrecursionistas.backend.persistence.sql.CotizacionDAOSQL;

@Component
public class RepositorioCotizacionImpl implements RepositorioCotizacion {
    private final CotizacionDAOSQL dao;

    public RepositorioCotizacionImpl(CotizacionDAOSQL dao) {
        this.dao = dao;
    }

    @Override
    public Cotizacion guardar(Cotizacion cotizacion) {
        return dao.save(cotizacion);
    }

    @Override
    public Page<Cotizacion> buscarPorJugador(Long jugadorId, java.time.LocalDate desde,
                                             java.time.LocalDate hasta, Pageable pageable) {
        return dao.buscarPorJugador(jugadorId,
                desde == null ? null : desde.atStartOfDay(),
                hasta == null ? null : hasta.plusDays(1).atStartOfDay(), pageable);
    }

    @Override
    public Page<CotizacionDAOSQL.RankingProyeccion> buscarRanking(Long ligaId, String posicion,
                                                                   Pageable pageable) {
        return dao.buscarRanking(ligaId, posicion, pageable);
    }
}
