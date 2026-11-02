package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.*;
import unq.losrecursionistas.backend.service.impl.MercadoServiceImpl;
import unq.losrecursionistas.backend.service.interfaces.MatchingEngine;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RecargaCreditosRollbackTest {
    @Test
    void siFallaLaPersistenciaNoQuedaAuditoria() {
        RepositorioUsuario usuarios = mock(RepositorioUsuario.class);
        RepositorioAuditoriaTransaccional auditorias = mock(RepositorioAuditoriaTransaccional.class);
        Usuario usuario = new Usuario("owner", "clave", 10.0);
        usuario.setId(1L);
        when(usuarios.buscarPorIdBloqueado(1L)).thenReturn(usuario);
        doThrow(new IllegalStateException("fallo")).when(usuarios).crearUsuario(usuario);
        MercadoServiceImpl service = new MercadoServiceImpl(usuarios, mock(RepositorioJugador.class),
                mock(RepositorioCotizacion.class), mock(RepositorioTenenciaToken.class),
                mock(RepositorioOrden.class), mock(RepositorioTransaccionMercado.class),
                mock(MatchingEngine.class), auditorias);

        assertThatThrownBy(() -> service.recargarCreditos(usuario, 1L, BigDecimal.ONE))
                .isInstanceOf(IllegalStateException.class);
        verify(auditorias, never()).crear(any());
    }
}
