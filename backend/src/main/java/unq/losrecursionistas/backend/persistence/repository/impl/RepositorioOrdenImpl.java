package unq.losrecursionistas.backend.persistence.repository.impl;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.model.*;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioOrden;
import unq.losrecursionistas.backend.persistence.sql.OrdenDAOSQL;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
    public Page<Orden> buscarActivas(Long jugadorId, TipoOrden tipo, String jugadorNombre,
                                     Pageable pageable) {
        return dao.buscarActivasConNombre(jugadorId, tipo, jugadorNombre, EstadoOrden.ACTIVA, pageable);
    }
    public Page<Orden> buscarPorUsuario(Long usuarioId, Pageable pageable) {
        return dao.findByUsuarioId(usuarioId, pageable);
    }
    public Orden buscarLiquidezInicialBloqueada(Long usuarioId, Long jugadorId) {
        return dao.findFirstByUsuarioIdAndJugadorIdAndEsLiquidezInicialTrue(
                usuarioId, jugadorId).orElse(null);
    }
    public List<ActividadDiariaMercado> contarOrdenesCreadasPorDia(LocalDate desde, LocalDate hasta) {
        AgregadosDiarios.validarRango(desde, hasta);
        Map<LocalDate, long[]> porDia = new TreeMap<>();
        for (OrdenDAOSQL.OrdenesPorDiaProyeccion fila : dao.contarCreadasPorDia(desde, hasta)) {
            long[] conteo = porDia.computeIfAbsent(AgregadosDiarios.dia(fila.getDia()), d -> new long[2]);
            int indice = TipoOrden.COMPRA.name().equals(fila.getTipo()) ? 0 : 1;
            conteo[indice] += AgregadosDiarios.entero(fila.getCantidad());
        }
        return porDia.entrySet().stream()
                .map(e -> new ActividadDiariaMercado(e.getKey(), e.getValue()[0], e.getValue()[1], 0,
                        ActividadDiariaMercado.vacia(e.getKey()).volumenCreditos()))
                .toList();
    }
}
