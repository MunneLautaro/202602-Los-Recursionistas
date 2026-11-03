package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import unq.losrecursionistas.backend.persistence.repository.interfaces.*;
import unq.losrecursionistas.backend.service.impl.MercadoServiceImpl;
import unq.losrecursionistas.backend.service.interfaces.MatchingEngine;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MercadoConsultaAutorizacionTest {
    @Test
    void historialSeSolicitaConElUsuarioAutorizadoYNoConDatosDeOtroUsuario() {
        RepositorioTransaccionMercado transacciones = mock(RepositorioTransaccionMercado.class);
        when(transacciones.buscarPorUsuario(eq(4L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        RepositorioUsuario usuarios = mock(RepositorioUsuario.class);
        RepositorioJugador jugadores = mock(RepositorioJugador.class);
        RepositorioCotizacion cotizaciones = mock(RepositorioCotizacion.class);
        RepositorioTenenciaToken tenencias = mock(RepositorioTenenciaToken.class);
        RepositorioOrden ordenes = mock(RepositorioOrden.class);
        MatchingEngine matchingEngine = mock(MatchingEngine.class);
        MercadoServiceImpl service = new MercadoServiceImpl(usuarios, jugadores, cotizaciones,
                tenencias, ordenes, transacciones, matchingEngine);

        service.listarTransacciones(4L, null);

        verify(transacciones).buscarPorUsuario(eq(4L), any(Pageable.class));
        verify(transacciones, never()).buscarPorUsuario(eq(9L), any(Pageable.class));
    }

    @Test
    void paginaConTamanoCeroUsaElTamanoPorDefecto() {
        RepositorioTransaccionMercado transacciones = mock(RepositorioTransaccionMercado.class);
        when(transacciones.buscarPorUsuario(eq(4L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        RepositorioUsuario usuarios = mock(RepositorioUsuario.class);
        RepositorioJugador jugadores = mock(RepositorioJugador.class);
        RepositorioCotizacion cotizaciones = mock(RepositorioCotizacion.class);
        RepositorioTenenciaToken tenencias = mock(RepositorioTenenciaToken.class);
        RepositorioOrden ordenes = mock(RepositorioOrden.class);
        MatchingEngine matchingEngine = mock(MatchingEngine.class);
        MercadoServiceImpl service = new MercadoServiceImpl(usuarios, jugadores, cotizaciones,
                tenencias, ordenes, transacciones, matchingEngine);

        service.listarTransacciones(4L, org.springframework.data.domain.PageRequest.of(0, 12));

        verify(transacciones).buscarPorUsuario(eq(4L), argThat(page -> page.getPageSize() == 12));
    }
}
