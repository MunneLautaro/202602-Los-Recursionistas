package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import unq.losrecursionistas.backend.model.Orden;
import unq.losrecursionistas.backend.model.TipoOrden;
import unq.losrecursionistas.backend.persistence.repository.interfaces.*;
import unq.losrecursionistas.backend.service.impl.MercadoServiceImpl;
import unq.losrecursionistas.backend.service.interfaces.MatchingEngine;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        Pageable invalidPage = mock(Pageable.class);
        when(invalidPage.getPageNumber()).thenReturn(-1);
        when(invalidPage.getPageSize()).thenReturn(12);
        assertThatThrownBy(() -> service.listarTransacciones(7L, invalidPage))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static MercadoServiceImpl service(RepositorioOrden ordenes,
                                               RepositorioTransaccionMercado transacciones) {
        RepositorioUsuario usuarios = mock(RepositorioUsuario.class);
        RepositorioJugador jugadores = mock(RepositorioJugador.class);
        RepositorioCotizacion cotizaciones = mock(RepositorioCotizacion.class);
        RepositorioTenenciaToken tenencias = mock(RepositorioTenenciaToken.class);
        MatchingEngine matchingEngine = mock(MatchingEngine.class);
        return new MercadoServiceImpl(usuarios, jugadores, cotizaciones, tenencias,
                ordenes, transacciones, matchingEngine);
    }
}
