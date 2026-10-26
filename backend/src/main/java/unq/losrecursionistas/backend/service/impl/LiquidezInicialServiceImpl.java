package unq.losrecursionistas.backend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import unq.losrecursionistas.backend.exceptions.ExcepcionCotizacionNoEncontrada;
import unq.losrecursionistas.backend.exceptions.ExcepcionJugadorInactivo;
import unq.losrecursionistas.backend.model.*;
import unq.losrecursionistas.backend.persistence.repository.interfaces.*;
import unq.losrecursionistas.backend.service.interfaces.LiquidezInicialService;

@Service
public class LiquidezInicialServiceImpl implements LiquidezInicialService {
    private static final int EMISION_INICIAL = 100;
    private final RepositorioUsuario usuarios;
    private final RepositorioCotizacion cotizaciones;
    private final RepositorioTenenciaToken tenencias;
    private final RepositorioOrden ordenes;

    public LiquidezInicialServiceImpl(RepositorioUsuario usuarios,
                                      RepositorioCotizacion cotizaciones,
                                      RepositorioTenenciaToken tenencias,
                                      RepositorioOrden ordenes) {
        this.usuarios = usuarios;
        this.cotizaciones = cotizaciones;
        this.tenencias = tenencias;
        this.ordenes = ordenes;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void inicializar(Jugador jugador) {
        if (jugador == null || !jugador.isActivo()) {
            throw new ExcepcionJugadorInactivo();
        }
        Cotizacion cotizacion = cotizaciones.vigente(jugador.getId());
        if (cotizacion == null) {
            throw new ExcepcionCotizacionNoEncontrada();
        }
        Usuario custodio = usuarios.buscarCustodio();
        if (ordenes.buscarLiquidezInicialBloqueada(custodio.getId(), jugador.getId()) != null) {
            return;
        }
        TenenciaToken tenencia = tenencias.buscarBloqueada(custodio.getId(), jugador.getId());
        if (tenencia == null) {
            tenencia = TenenciaToken.builder().usuario(custodio).jugador(jugador)
                    .cantidadTotal(0).cantidadReservada(0).build();
        }
        if (tenencia.getCantidadTotal() < EMISION_INICIAL) {
            tenencia.acreditar(EMISION_INICIAL - tenencia.getCantidadTotal());
        }
        int reservaFaltante = EMISION_INICIAL - tenencia.getCantidadReservada();
        if (reservaFaltante > 0) {
            tenencia.reservar(reservaFaltante);
        }
        tenencias.guardar(tenencia);
        ordenes.guardar(Orden.crear(custodio, jugador, TipoOrden.VENTA, EMISION_INICIAL,
                cotizacion.getValor(), true));
    }
}
