package unq.losrecursionistas.backend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unq.losrecursionistas.backend.model.*;
import unq.losrecursionistas.backend.persistence.repository.interfaces.*;
import unq.losrecursionistas.backend.service.interfaces.MatchingEngine;

import java.math.BigDecimal;

@Service
public class MatchingEngineImpl implements MatchingEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatchingEngineImpl.class);

    private final RepositorioOrden ordenes;
    private final RepositorioTenenciaToken tenencias;
    private final RepositorioTransaccionMercado transacciones;
    private final RepositorioAuditoriaTransaccional auditorias;

    public MatchingEngineImpl(RepositorioOrden ordenes, RepositorioTenenciaToken tenencias,
                              RepositorioTransaccionMercado transacciones,
                              RepositorioAuditoriaTransaccional auditorias) {
        this.ordenes = ordenes;
        this.tenencias = tenencias;
        this.transacciones = transacciones;
        this.auditorias = auditorias;
    }

    @Override
    @Transactional
    public void ejecutar(Orden orden) {
        TipoOrden opuesto = orden.getTipo() == TipoOrden.COMPRA ? TipoOrden.VENTA : TipoOrden.COMPRA;
        for (Orden compatible : ordenes.buscarCompatiblesBloqueadas(
                orden.getJugador().getId(), opuesto, orden.getPrecioUnitario(),
                orden.getUsuario().getId())) {
            if (orden.getEstado() != EstadoOrden.ACTIVA) {
                break;
            }
            int cantidad = Math.min(orden.getCantidadDisponible(),
                    compatible.getCantidadDisponible());
            liquidar(orden, compatible, cantidad);
        }
    }

    private void liquidar(Orden primera, Orden segunda, int cantidad) {
        Orden compra = primera.getTipo() == TipoOrden.COMPRA ? primera : segunda;
        Orden venta = primera.getTipo() == TipoOrden.VENTA ? primera : segunda;
        BigDecimal total = compra.getPrecioUnitario().multiply(BigDecimal.valueOf(cantidad));
        Usuario comprador = compra.getUsuario();
        Usuario vendedor = venta.getUsuario();
        TenenciaToken tenenciaVendedor = tenencias.buscarBloqueada(
                vendedor.getId(), venta.getJugador().getId());
        if (tenenciaVendedor == null) {
            throw new IllegalStateException("La orden de venta no tiene tenencia");
        }
        tenenciaVendedor.liberar(cantidad);
        tenenciaVendedor.debitar(cantidad);
        TenenciaToken tenenciaComprador = tenencias.buscarBloqueada(
                comprador.getId(), compra.getJugador().getId());
        if (tenenciaComprador == null) {
            tenenciaComprador = TenenciaToken.builder().usuario(comprador)
                    .jugador(compra.getJugador()).cantidadTotal(0).cantidadReservada(0).build();
        }
        tenenciaComprador.acreditar(cantidad);
        comprador.liberar(total.doubleValue());
        comprador.debitar(total.doubleValue());
        vendedor.acreditar(total.doubleValue());
        primera.ejecutar(cantidad);
        segunda.ejecutar(cantidad);
        tenencias.guardar(tenenciaVendedor);
        tenencias.guardar(tenenciaComprador);
        ordenes.guardar(primera);
        ordenes.guardar(segunda);
        TransaccionMercado transaccion = transacciones.crear(
                TransaccionMercado.crear(compra, venta, cantidad));
        auditorias.crear(AuditoriaTransaccional.crear("TRANSACCION_MERCADO",
                transaccion.getId(), comprador, "Liquidacion de match",
                "ordenes activas; reservas pendientes",
                "cantidad=" + cantidad + ";total=" + total));

        LOGGER.info("Transacción de mercado ejecutada con éxito [ID: {}]: Comprador '{}' adquirió {} tokens de '{}' del vendedor '{}' a ${} c/u (Total: ${}).",
                transaccion.getId(), comprador.getNombreUsuario(), cantidad, compra.getJugador().getNombre(),
                vendedor.getNombreUsuario(), compra.getPrecioUnitario(), total);
    }
}
