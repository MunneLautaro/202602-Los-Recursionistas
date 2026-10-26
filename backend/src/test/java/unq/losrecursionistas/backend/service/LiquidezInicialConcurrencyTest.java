package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.model.*;
import unq.losrecursionistas.backend.persistence.repository.interfaces.*;
import unq.losrecursionistas.backend.service.impl.LiquidezInicialServiceImpl;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LiquidezInicialConcurrencyTest {
    @Test
    void laConsultaBloqueadaSeRealizaAntesDeCrearLiquidez() {
        RepositorioUsuario usuarios = mock(RepositorioUsuario.class);
        RepositorioCotizacion cotizaciones = mock(RepositorioCotizacion.class);
        RepositorioTenenciaToken tenencias = mock(RepositorioTenenciaToken.class);
        RepositorioOrden ordenes = mock(RepositorioOrden.class);
        Usuario custodio = new Usuario("custodio", "clave", 0.0);
        custodio.setId(1L);
        Jugador jugador = new Jugador("Jugador", "DEL", null, true);
        jugador.setId(2L);
        when(usuarios.buscarCustodio()).thenReturn(custodio);
        when(cotizaciones.vigente(2L)).thenReturn(Cotizacion.crear(jugador,
                new BigDecimal("5.00"), java.time.LocalDateTime.now(), "test",
                java.time.LocalDate.now(), java.time.LocalDate.now()));
        when(ordenes.buscarLiquidezInicialBloqueada(1L, 2L)).thenReturn(null);
        when(tenencias.buscarBloqueada(1L, 2L)).thenReturn(null);
        LiquidezInicialServiceImpl service = new LiquidezInicialServiceImpl(
                usuarios, cotizaciones, tenencias, ordenes);

        service.inicializar(jugador);

        verify(ordenes).buscarLiquidezInicialBloqueada(1L, 2L);
        verify(ordenes).guardar(any(Orden.class));
        verify(ordenes, times(1)).guardar(any());
    }
}
