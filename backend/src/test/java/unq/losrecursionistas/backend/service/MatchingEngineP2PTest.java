package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.model.*;
import unq.losrecursionistas.backend.persistence.repository.interfaces.*;
import unq.losrecursionistas.backend.service.impl.MatchingEngineImpl;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MatchingEngineP2PTest {
    @Test
    void procesaOrdenesP2PFIFOYMantieneRemanente() {
        RepositorioOrden ordenes = mock(RepositorioOrden.class);
        RepositorioTenenciaToken tenencias = mock(RepositorioTenenciaToken.class);
        RepositorioTransaccionMercado transacciones = mock(RepositorioTransaccionMercado.class);
        RepositorioAuditoriaTransaccional auditorias = mock(RepositorioAuditoriaTransaccional.class);
        when(transacciones.crear(any())).thenAnswer(invocation -> invocation.getArgument(0));
        MatchingEngineImpl engine = new MatchingEngineImpl(ordenes, tenencias, transacciones, auditorias);
        Usuario comprador = usuario("comprador", 100, 1L);
        Usuario vendedor = usuario("vendedor", 0, 2L);
        Jugador jugador = jugador(10L);
        Orden compra = Orden.crear(comprador, jugador, TipoOrden.COMPRA, 7,
                new BigDecimal("10.00"), false);
        Orden ventaAnterior = Orden.crear(vendedor, jugador, TipoOrden.VENTA, 4,
                new BigDecimal("10.00"), false);
        Orden ventaPosterior = Orden.crear(vendedor, jugador, TipoOrden.VENTA, 6,
                new BigDecimal("10.00"), false);
        var fechaAnterior = ventaAnterior.getFechaCreacion();
        comprador.reservar(70.0);
        TenenciaToken tenencia = TenenciaToken.builder().usuario(vendedor).jugador(jugador)
                .cantidadTotal(10).cantidadReservada(10).build();
        when(ordenes.buscarCompatiblesBloqueadas(10L, TipoOrden.VENTA,
                new BigDecimal("10.00"), 1L)).thenReturn(List.of(ventaAnterior, ventaPosterior));
        when(tenencias.buscarBloqueada(2L, 10L)).thenReturn(tenencia);
        when(tenencias.buscarBloqueada(1L, 10L)).thenReturn(null);

        engine.ejecutar(compra);

        assertThat(ventaAnterior.getEstado()).isEqualTo(EstadoOrden.COMPLETADA);
        assertThat(ventaPosterior.getCantidadDisponible()).isEqualTo(3);
        assertThat(ventaAnterior.getFechaCreacion()).isEqualTo(fechaAnterior);
        assertThat(tenencia.getCantidadTotal()).isEqualTo(3);
    }

    private static Usuario usuario(String nombre, double saldo, long id) {
        Usuario usuario = new Usuario(nombre, "clave", saldo);
        usuario.setId(id);
        return usuario;
    }
    private static Jugador jugador(long id) {
        Jugador jugador = new Jugador("Jugador", "DEL", null, true);
        jugador.setId(id);
        return jugador;
    }
}
