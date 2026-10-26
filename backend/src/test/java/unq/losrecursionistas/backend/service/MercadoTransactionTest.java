package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.model.*;
import unq.losrecursionistas.backend.persistence.repository.interfaces.*;
import unq.losrecursionistas.backend.service.impl.MatchingEngineImpl;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MercadoTransactionTest {
    @Test
    void fallaAntesDeGuardarTransaccionSiLaVentaNoTieneTenencia() {
        RepositorioOrden ordenes = mock(RepositorioOrden.class);
        RepositorioTenenciaToken tenencias = mock(RepositorioTenenciaToken.class);
        RepositorioTransaccionMercado transacciones = mock(RepositorioTransaccionMercado.class);
        RepositorioAuditoriaTransaccional auditorias = mock(RepositorioAuditoriaTransaccional.class);
        MatchingEngineImpl engine = new MatchingEngineImpl(ordenes, tenencias, transacciones, auditorias);
        Usuario comprador = usuario("comprador", 100, 1L);
        Usuario vendedor = usuario("vendedor", 0, 2L);
        Jugador jugador = new Jugador("Jugador", "DEL", null, true);
        jugador.setId(10L);
        Orden compra = Orden.crear(comprador, jugador, TipoOrden.COMPRA, 2,
                new BigDecimal("10.00"), false);
        Orden venta = Orden.crear(vendedor, jugador, TipoOrden.VENTA, 2,
                new BigDecimal("10.00"), false);
        comprador.reservar(20.0);
        when(ordenes.buscarCompatiblesBloqueadas(10L, TipoOrden.VENTA,
                new BigDecimal("10.00"), 1L)).thenReturn(List.of(venta));
        when(tenencias.buscarBloqueada(2L, 10L)).thenReturn(null);

        assertThatThrownBy(() -> engine.ejecutar(compra))
                .isInstanceOf(IllegalStateException.class);
        verify(transacciones, never()).crear(any());
        verify(auditorias, never()).crear(any());
    }

    @Test
    void unaSegundaReservaDeLaMismaTenenciaEsRechazada() {
        Usuario usuario = usuario("vendedor", 0, 2L);
        Jugador jugador = new Jugador("Jugador", "DEL", null, true);
        TenenciaToken tenencia = TenenciaToken.builder().usuario(usuario).jugador(jugador)
                .cantidadTotal(10).cantidadReservada(0).build();

        tenencia.reservar(10);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> tenencia.reservar(1))
                .isInstanceOf(unq.losrecursionistas.backend.exceptions.ExcepcionRecursosInsuficientes.class);
    }

    private static Usuario usuario(String nombre, double saldo, long id) {
        Usuario usuario = new Usuario(nombre, "clave", saldo);
        usuario.setId(id);
        return usuario;
    }
}
