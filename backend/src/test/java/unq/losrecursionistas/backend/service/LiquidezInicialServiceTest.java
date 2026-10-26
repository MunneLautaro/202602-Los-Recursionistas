package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import unq.losrecursionistas.backend.exceptions.ExcepcionCotizacionNoEncontrada;
import unq.losrecursionistas.backend.exceptions.ExcepcionJugadorInactivo;
import unq.losrecursionistas.backend.model.*;
import unq.losrecursionistas.backend.persistence.repository.interfaces.*;
import unq.losrecursionistas.backend.service.impl.LiquidezInicialServiceImpl;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LiquidezInicialServiceTest {
    private RepositorioUsuario usuarios;
    private RepositorioCotizacion cotizaciones;
    private RepositorioTenenciaToken tenencias;
    private RepositorioOrden ordenes;
    private LiquidezInicialServiceImpl service;
    private Usuario custodio;
    private Jugador jugador;

    @BeforeEach
    void setUp() {
        usuarios = mock(RepositorioUsuario.class);
        cotizaciones = mock(RepositorioCotizacion.class);
        tenencias = mock(RepositorioTenenciaToken.class);
        ordenes = mock(RepositorioOrden.class);
        service = new LiquidezInicialServiceImpl(usuarios, cotizaciones, tenencias, ordenes);
        custodio = new Usuario("custodio", "clave", 0.0);
        custodio.setId(99L);
        jugador = new Jugador("Jugador", "DEL", null, true);
        jugador.setId(10L);
        when(usuarios.buscarCustodio()).thenReturn(custodio);
        when(cotizaciones.vigente(10L)).thenReturn(Cotizacion.crear(jugador,
                new BigDecimal("15.00"), java.time.LocalDateTime.now(), "test",
                java.time.LocalDate.now(), java.time.LocalDate.now()));
        when(tenencias.buscarBloqueada(99L, 10L)).thenReturn(null);
    }

    @Test
    void emiteCienTokensYUnaOrdenInicialConPrecioVigente() {
        service.inicializar(jugador);

        ArgumentCaptor<TenenciaToken> tenencia = ArgumentCaptor.forClass(TenenciaToken.class);
        ArgumentCaptor<Orden> orden = ArgumentCaptor.forClass(Orden.class);
        verify(tenencias).guardar(tenencia.capture());
        verify(ordenes).guardar(orden.capture());
        assertThat(tenencia.getValue().getCantidadTotal()).isEqualTo(100);
        assertThat(tenencia.getValue().getCantidadReservada()).isEqualTo(100);
        assertThat(orden.getValue().isEsLiquidezInicial()).isTrue();
        assertThat(orden.getValue().getPrecioUnitario()).isEqualByComparingTo("15.00");
    }

    @Test
    void inicializacionRepetidaNoModificaLaOrdenExistente() {
        Orden existente = Orden.crear(custodio, jugador, TipoOrden.VENTA, 100,
                new BigDecimal("15.00"), true);
        when(ordenes.buscarLiquidezInicialBloqueada(99L, 10L)).thenReturn(existente);

        service.inicializar(jugador);

        verify(ordenes, never()).guardar(any());
        verify(tenencias, never()).guardar(any());
    }

    @Test
    void rechazaJugadorInactivoYSinCotizacion() {
        jugador.setActivo(false);
        assertThatThrownBy(() -> service.inicializar(jugador))
                .isInstanceOf(ExcepcionJugadorInactivo.class);
        jugador.setActivo(true);
        when(cotizaciones.vigente(10L)).thenReturn(null);
        assertThatThrownBy(() -> service.inicializar(jugador))
                .isInstanceOf(ExcepcionCotizacionNoEncontrada.class);
    }
}
