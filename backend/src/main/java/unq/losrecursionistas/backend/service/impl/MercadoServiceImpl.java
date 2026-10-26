package unq.losrecursionistas.backend.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import unq.losrecursionistas.backend.exceptions.ExcepcionCotizacionNoEncontrada;
import unq.losrecursionistas.backend.exceptions.ExcepcionJugadorInactivo;
import unq.losrecursionistas.backend.exceptions.ExcepcionRecursosInsuficientes;
import unq.losrecursionistas.backend.model.*;
import unq.losrecursionistas.backend.persistence.repository.interfaces.*;
import unq.losrecursionistas.backend.service.interfaces.MatchingEngine;
import unq.losrecursionistas.backend.service.interfaces.MercadoService;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioAuditoriaTransaccional;

import java.math.BigDecimal;

@Service
public class MercadoServiceImpl implements MercadoService {
    private static final int PAGINA_DEFECTO = 12;

    private final RepositorioUsuario usuarios;
    private final RepositorioJugador jugadores;
    private final RepositorioCotizacion cotizaciones;
    private final RepositorioTenenciaToken tenencias;
    private final RepositorioOrden ordenes;
    private final RepositorioTransaccionMercado transacciones;
    private final MatchingEngine matching;
    private final RepositorioAuditoriaTransaccional auditorias;

    public MercadoServiceImpl(RepositorioUsuario usuarios, RepositorioJugador jugadores,
                              RepositorioCotizacion cotizaciones,
                              RepositorioTenenciaToken tenencias, RepositorioOrden ordenes,
                              RepositorioTransaccionMercado transacciones,
                              MatchingEngine matching) {
        this(usuarios, jugadores, cotizaciones, tenencias, ordenes, transacciones,
                matching, null);
    }

    @Autowired
    public MercadoServiceImpl(RepositorioUsuario usuarios, RepositorioJugador jugadores,
                              RepositorioCotizacion cotizaciones,
                              RepositorioTenenciaToken tenencias, RepositorioOrden ordenes,
                              RepositorioTransaccionMercado transacciones,
                              MatchingEngine matching,
                              RepositorioAuditoriaTransaccional auditorias) {
        this.usuarios = usuarios;
        this.jugadores = jugadores;
        this.cotizaciones = cotizaciones;
        this.tenencias = tenencias;
        this.ordenes = ordenes;
        this.transacciones = transacciones;
        this.matching = matching;
        this.auditorias = auditorias;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Orden crearOrden(Usuario usuario, Long jugadorId, int cantidad, TipoOrden tipo) {
        if (usuario == null || tipo == null || cantidad <= 0) {
            throw new ExcepcionRecursosInsuficientes("Los datos de la orden son invalidos");
        }
        Usuario usuarioBloqueado = usuarios.buscarPorIdBloqueado(usuario.getId());
        Jugador jugador = jugadores.obtenerJugadorPorId(jugadorId);
        if (!jugador.isActivo()) {
            throw new ExcepcionJugadorInactivo();
        }
        var cotizacion = cotizaciones.vigente(jugadorId);
        if (cotizacion == null || cotizacion.getValor() == null
                || cotizacion.getValor().signum() <= 0) {
            throw new ExcepcionCotizacionNoEncontrada();
        }
        BigDecimal precio = cotizacion.getValor();
        if (tipo == TipoOrden.COMPRA) {
            usuarioBloqueado.reservar(precio.multiply(BigDecimal.valueOf(cantidad)).doubleValue());
        } else {
            TenenciaToken tenencia = tenencias.buscarBloqueada(usuarioBloqueado.getId(), jugadorId);
            if (tenencia == null) {
                throw new ExcepcionRecursosInsuficientes("No hay tokens disponibles");
            }
            tenencia.reservar(cantidad);
            tenencias.guardar(tenencia);
        }
        Orden orden = ordenes.guardar(Orden.crear(usuarioBloqueado, jugador, tipo, cantidad,
                precio, false));
        matching.ejecutar(orden);
        return orden;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Orden> listarOrdenes(Long jugadorId, TipoOrden tipo, Pageable pageable) {
        return ordenes.buscarActivas(jugadorId, tipo, normalizarPagina(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransaccionMercado> listarTransacciones(Long usuarioId, Pageable pageable) {
        return transacciones.buscarPorUsuario(usuarioId, normalizarPagina(pageable));
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Usuario recargarCreditos(Usuario solicitante, Long usuarioId, BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser positivo");
        }
        if (solicitante == null || usuarioId == null
                || (!usuarioId.equals(solicitante.getId())
                && !solicitante.getAutoridades().contains("ROLE_ADMIN"))) {
            throw new AccessDeniedException("No puede modificar otra cuenta");
        }
        Usuario usuario = usuarios.buscarPorIdBloqueado(usuarioId);
        if (usuario == null) {
            throw new IllegalArgumentException("La cuenta no existe");
        }
        double saldoAnterior = usuario.getSaldo();
        usuario.acreditar(monto.doubleValue());
        usuarios.crearUsuario(usuario);
        if (auditorias != null) {
            auditorias.crear(AuditoriaTransaccional.crear("USUARIO", usuario.getId(),
                    solicitante, "Recarga de creditos",
                    "saldo=" + saldoAnterior, "saldo=" + usuario.getSaldo()));
        }
        return usuario;
    }

    private Pageable normalizarPagina(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(0, PAGINA_DEFECTO, Sort.by("fechaCreacion").ascending());
        }
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() < 1) {
            throw new IllegalArgumentException("La paginacion es invalida");
        }
        return pageable.getPageSize() == 0
                ? PageRequest.of(pageable.getPageNumber(), PAGINA_DEFECTO)
                : pageable;
    }

}
