package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.model.*;
import unq.losrecursionistas.backend.persistence.repository.interfaces.*;
import unq.losrecursionistas.backend.service.impl.MatchingEngineImpl;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MatchingEngineTest {
    @Test
    void ejecutaElMinimoCompletaLaOrdenMenorYConservaFIFO() {
        RepositorioOrden ordenes = mock(RepositorioOrden.class);
        RepositorioTenenciaToken tenencias = mock(RepositorioTenenciaToken.class);
        RepositorioTransaccionMercado transacciones = mock(RepositorioTransaccionMercado.class);
        RepositorioAuditoriaTransaccional auditorias = mock(RepositorioAuditoriaTransaccional.class);
        MatchingEngineImpl engine = new MatchingEngineImpl(ordenes, tenencias, transacciones, auditorias);
        when(transacciones.crear(any(TransaccionMercado.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario comprador = usuario("comprador", 1000, 1L);
        Usuario vendedor = usuario("vendedor", 0, 2L);
        Jugador jugador = jugador(10L);
        Orden compra = Orden.crear(comprador, jugador, TipoOrden.COMPRA, 5,
                new BigDecimal("10.00"), false);
        Orden venta = Orden.crear(vendedor, jugador, TipoOrden.VENTA, 8,
                new BigDecimal("10.00"), false);
        TenenciaToken tenencia = TenenciaToken.builder().usuario(vendedor).jugador(jugador)
                .cantidadTotal(8).cantidadReservada(8).build();
        comprador.reservar(50.0);
        when(ordenes.buscarCompatiblesBloqueadas(10L, TipoOrden.VENTA,
                new BigDecimal("10.00"), 1L)).thenReturn(List.of(venta));
        when(tenencias.buscarBloqueada(2L, 10L)).thenReturn(tenencia);
        when(tenencias.buscarBloqueada(1L, 10L)).thenReturn(null);

        engine.ejecutar(compra);

        assertThat(compra.getEstado()).isEqualTo(EstadoOrden.COMPLETADA);
        assertThat(venta.getCantidadDisponible()).isEqualTo(3);
        assertThat(venta.getFechaCreacion()).isNotNull();
        assertThat(tenencia.getCantidadTotal()).isEqualTo(3);
        assertThat(comprador.getSaldo()).isEqualTo(950.0);
        assertThat(vendedor.getSaldo()).isEqualTo(50.0);
        verify(transacciones).crear(any(TransaccionMercado.class));
    }

    @Test
    void solicitaAlRepositorioSoloElJugadorPrecioYUsuarioCompatibles() {
        RepositorioOrden ordenes = mock(RepositorioOrden.class);
        MatchingEngineImpl engine = new MatchingEngineImpl(ordenes, mock(RepositorioTenenciaToken.class),
                mock(RepositorioTransaccionMercado.class), mock(RepositorioAuditoriaTransaccional.class));
        Usuario comprador = usuario("comprador", 100, 1L);
        Jugador jugador = jugador(10L);
        Orden compra = Orden.crear(comprador, jugador, TipoOrden.COMPRA, 1,
                new BigDecimal("8.00"), false);
        when(ordenes.buscarCompatiblesBloqueadas(10L, TipoOrden.VENTA,
                new BigDecimal("8.00"), 1L)).thenReturn(List.of());

        engine.ejecutar(compra);

        verify(ordenes).buscarCompatiblesBloqueadas(10L, TipoOrden.VENTA,
                new BigDecimal("8.00"), 1L);
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
