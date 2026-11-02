package unq.losrecursionistas.backend.persistence.repository.impl;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.model.*;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioOrden;
import unq.losrecursionistas.backend.persistence.sql.OrdenDAOSQL;
import java.math.BigDecimal;
import java.util.List;

@Component
public class RepositorioOrdenImpl implements RepositorioOrden {
    private final OrdenDAOSQL dao;
    public RepositorioOrdenImpl(OrdenDAOSQL dao) { this.dao = dao; }
    public Orden guardar(Orden orden) { return dao.save(orden); }
    public List<Orden> buscarCompatiblesBloqueadas(Long jugadorId, TipoOrden tipo,
                                                    BigDecimal precio, Long usuarioId) {
        return dao.buscarCompatiblesBloqueadas(jugadorId, tipo, precio, EstadoOrden.ACTIVA, usuarioId);
    }
    public Page<Orden> buscarActivas(Long jugadorId, TipoOrden tipo, Pageable pageable) {
        if (jugadorId != null && tipo != null) return dao.findByJugadorIdAndTipoAndEstado(jugadorId, tipo, EstadoOrden.ACTIVA, pageable);
        if (jugadorId != null) return dao.findByJugadorIdAndEstado(jugadorId, EstadoOrden.ACTIVA, pageable);
        if (tipo != null) return dao.findByTipoAndEstado(tipo, EstadoOrden.ACTIVA, pageable);
        return dao.findByEstado(EstadoOrden.ACTIVA, pageable);
    }
    public Orden buscarLiquidezInicialBloqueada(Long usuarioId, Long jugadorId) {
        return dao.findFirstByUsuarioIdAndJugadorIdAndEsLiquidezInicialTrue(
                usuarioId, jugadorId).orElse(null);
    }
}
