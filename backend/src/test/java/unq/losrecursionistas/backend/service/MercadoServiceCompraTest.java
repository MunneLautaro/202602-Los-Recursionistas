package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unq.losrecursionistas.backend.exceptions.ExcepcionCotizacionNoEncontrada;
import unq.losrecursionistas.backend.exceptions.ExcepcionJugadorInactivo;
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
class MercadoServiceCompraTest {
    @Mock RepositorioUsuario usuarios;
    @Mock RepositorioJugador jugadores;
    @Mock RepositorioCotizacion cotizaciones;
    @Mock RepositorioTenenciaToken tenencias;
    @Mock RepositorioOrden ordenes;
    @Mock RepositorioTransaccionMercado transacciones;
    @Mock MatchingEngine matching;
    private MercadoServiceImpl service;
    private Usuario comprador;
    private Jugador jugador;

    @BeforeEach
    void setUp() {
        service = new MercadoServiceImpl(usuarios, jugadores, cotizaciones, tenencias,
                ordenes, transacciones, matching);
        comprador = new Usuario("comprador", "clave", 1000.0);
        comprador.setId(1L);
        jugador = new Jugador("Jugador", "DEL", null, true);
        jugador.setId(10L);
        lenient().when(usuarios.buscarPorIdBloqueado(1L)).thenReturn(comprador);
        lenient().when(jugadores.obtenerJugadorPorId(10L)).thenReturn(jugador);
        lenient().when(cotizaciones.vigente(10L)).thenReturn(Cotizacion.crear(jugador,
                new BigDecimal("12.50"), java.time.LocalDateTime.now(),
                "test", java.time.LocalDate.now(), java.time.LocalDate.now()));
        lenient().when(ordenes.guardar(any(Orden.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void creaCompraParcialAlPrecioVigenteYReservaElTotal() {
        Orden orden = service.crearOrden(comprador, 10L, 5, TipoOrden.COMPRA);

        assertThat(orden.getCantidadOriginal()).isEqualTo(5);
        assertThat(orden.getPrecioUnitario()).isEqualByComparingTo("12.50");
        assertThat(comprador.getSaldoReservado()).isEqualTo(62.50);
        verify(matching).ejecutar(orden);
    }

    @Test
    void rechazaCompraSinSaldoYNoCreaOrden() {
        comprador.setSaldo(10.0);

        assertThatThrownBy(() -> service.crearOrden(comprador, 10L, 1, TipoOrden.COMPRA))
                .isInstanceOf(unq.losrecursionistas.backend.exceptions.ExcepcionDominio.class);

        verify(ordenes, never()).guardar(any());
        assertThat(comprador.getSaldo()).isEqualTo(10.0);
        assertThat(comprador.getSaldoReservado()).isZero();
    }

    @Test
    void rechazaJugadorInactivoYSinCotizacion() {
        jugador.setActivo(false);
        assertThatThrownBy(() -> service.crearOrden(comprador, 10L, 1, TipoOrden.COMPRA))
                .isInstanceOf(ExcepcionJugadorInactivo.class);

        jugador.setActivo(true);
        when(cotizaciones.vigente(10L)).thenReturn(null);
        assertThatThrownBy(() -> service.crearOrden(comprador, 10L, 1, TipoOrden.COMPRA))
                .isInstanceOf(ExcepcionCotizacionNoEncontrada.class);
        verify(ordenes, never()).guardar(any());
    }
}
