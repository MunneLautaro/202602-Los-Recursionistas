package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import unq.losrecursionistas.backend.model.*;
import unq.losrecursionistas.backend.persistence.repository.interfaces.*;
import unq.losrecursionistas.backend.service.impl.MercadoServiceImpl;
import unq.losrecursionistas.backend.service.interfaces.MatchingEngine;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MercadoConsultaServiceTest {
    @Test
    void usaPaginaPorDefectoYConservaFiltrosDelLibro() {
        RepositorioOrden ordenes = mock(RepositorioOrden.class);
        RepositorioTransaccionMercado transacciones = mock(RepositorioTransaccionMercado.class);
        MercadoServiceImpl service = service(ordenes, transacciones);
        Page<Orden> expected = new PageImpl<>(List.of());
        when(ordenes.buscarActivas(eq(10L), eq(TipoOrden.VENTA), any(Pageable.class)))
                .thenReturn(expected);

        assertThat(service.listarOrdenes(10L, TipoOrden.VENTA, null)).isSameAs(expected);
        verify(ordenes).buscarActivas(eq(10L), eq(TipoOrden.VENTA),
                argThat(page -> page.getPageNumber() == 0 && page.getPageSize() == 12));
    }

    @Test
    void paginaTransaccionesPorUsuarioYRechazaPaginacionInvalida() {
        RepositorioOrden ordenes = mock(RepositorioOrden.class);
        RepositorioTransaccionMercado transacciones = mock(RepositorioTransaccionMercado.class);
        MercadoServiceImpl service = service(ordenes, transacciones);
        when(transacciones.buscarPorUsuario(eq(7L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.listarTransacciones(7L, PageRequest.of(0, 12));
        verify(transacciones).buscarPorUsuario(eq(7L), argThat(page -> page.getPageSize() == 12));
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                service.listarTransacciones(7L, PageRequest.of(-1, 12)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static MercadoServiceImpl service(RepositorioOrden ordenes,
                                               RepositorioTransaccionMercado transacciones) {
        return new MercadoServiceImpl(mock(RepositorioUsuario.class), mock(RepositorioJugador.class),
                mock(RepositorioCotizacion.class), mock(RepositorioTenenciaToken.class),
                ordenes, transacciones, mock(MatchingEngine.class));
    }
}
