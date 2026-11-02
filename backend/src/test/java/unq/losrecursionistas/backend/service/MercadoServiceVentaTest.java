package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unq.losrecursionistas.backend.exceptions.ExcepcionCantidadInvalida;
import unq.losrecursionistas.backend.exceptions.ExcepcionRecursosInsuficientes;
import unq.losrecursionistas.backend.model.*;
import unq.losrecursionistas.backend.persistence.repository.interfaces.*;
import unq.losrecursionistas.backend.service.impl.MercadoServiceImpl;
import unq.losrecursionistas.backend.service.interfaces.MatchingEngine;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MercadoServiceVentaTest {
    @Mock RepositorioUsuario usuarios;
    @Mock RepositorioJugador jugadores;
    @Mock RepositorioCotizacion cotizaciones;
    @Mock RepositorioTenenciaToken tenencias;
    @Mock RepositorioOrden ordenes;
    @Mock RepositorioTransaccionMercado transacciones;
    @Mock MatchingEngine matching;
    private MercadoServiceImpl service;
    private Usuario vendedor;
    private Jugador jugador;
    private TenenciaToken tenencia;

    @BeforeEach
    void setUp() {
        service = new MercadoServiceImpl(usuarios, jugadores, cotizaciones, tenencias,
                ordenes, transacciones, matching);
        vendedor = new Usuario("vendedor", "clave", 0.0);
        vendedor.setId(2L);
        jugador = new Jugador("Jugador", "DEL", null, true);
        jugador.setId(10L);
        tenencia = TenenciaToken.builder().usuario(vendedor).jugador(jugador)
                .cantidadTotal(10).cantidadReservada(0).build();
        lenient().when(usuarios.buscarPorIdBloqueado(2L)).thenReturn(vendedor);
        lenient().when(jugadores.obtenerJugadorPorId(10L)).thenReturn(jugador);
        lenient().when(tenencias.buscarBloqueada(2L, 10L)).thenReturn(tenencia);
        lenient().when(cotizaciones.vigente(10L)).thenReturn(Cotizacion.crear(jugador,
                new BigDecimal("7.50"), java.time.LocalDateTime.now(), "test",
                java.time.LocalDate.now(), java.time.LocalDate.now()));
        lenient().when(ordenes.guardar(any(Orden.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void reservaTokensYPublicaVentaAlPrecioVigente() {
        Orden orden = service.crearOrden(vendedor, 10L, 4, TipoOrden.VENTA);

        assertThat(orden.getCantidadDisponible()).isEqualTo(4);
        assertThat(tenencia.getCantidadReservada()).isEqualTo(4);
        verify(tenencias).guardar(tenencia);
        verify(matching).ejecutar(orden);
    }

    @Test
    void rechazaVentaPorTokensInsuficientesSinCambios() {
        assertThatThrownBy(() -> service.crearOrden(vendedor, 10L, 11, TipoOrden.VENTA))
                .isInstanceOf(ExcepcionRecursosInsuficientes.class);

        assertThat(tenencia.getCantidadReservada()).isZero();
        verify(ordenes, never()).guardar(any());
    }

    @Test
    void rechazaCantidadCeroNegativaYFraccionariaRepresentadaComoEntero() {
        assertThatThrownBy(() -> service.crearOrden(vendedor, 10L, 0, TipoOrden.VENTA))
                .isInstanceOf(ExcepcionRecursosInsuficientes.class);
        assertThatThrownBy(() -> service.crearOrden(vendedor, 10L, -1, TipoOrden.VENTA))
                .isInstanceOf(ExcepcionRecursosInsuficientes.class);
        assertThatThrownBy(() -> tenencia.reservar(0))
                .isInstanceOf(ExcepcionCantidadInvalida.class);
        verify(ordenes, never()).guardar(any());
    }
}
