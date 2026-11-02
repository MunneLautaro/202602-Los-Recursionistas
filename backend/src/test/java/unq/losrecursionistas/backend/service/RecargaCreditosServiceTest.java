package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.*;
import unq.losrecursionistas.backend.service.impl.MercadoServiceImpl;
import unq.losrecursionistas.backend.service.interfaces.MatchingEngine;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecargaCreditosServiceTest {
    @Mock RepositorioUsuario usuarios;
    @Mock RepositorioJugador jugadores;
    @Mock RepositorioCotizacion cotizaciones;
    @Mock RepositorioTenenciaToken tenencias;
    @Mock RepositorioOrden ordenes;
    @Mock RepositorioTransaccionMercado transacciones;
    @Mock RepositorioAuditoriaTransaccional auditorias;
    @Mock MatchingEngine matching;
    private MercadoServiceImpl service;
    private Usuario owner;
    private Usuario other;

    @BeforeEach
    void setUp() {
        service = new MercadoServiceImpl(usuarios, jugadores, cotizaciones, tenencias,
                ordenes, transacciones, matching, auditorias);
        owner = new Usuario("owner", "clave", 10.0);
        owner.setId(1L);
        other = new Usuario("other", "clave", 20.0);
        other.setId(2L);
        lenient().when(usuarios.buscarPorIdBloqueado(1L)).thenReturn(owner);
    }

    @Test
    void acreditaMontoPositivoYRegistraAuditoria() {
        Usuario result = service.recargarCreditos(owner, 1L, new BigDecimal("25.50"));

        assertThat(result.getSaldo()).isEqualTo(35.50);
        verify(usuarios).buscarPorIdBloqueado(1L);
        verify(usuarios).crearUsuario(owner);
        verify(auditorias).crear(any());
    }

    @Test
    void rechazaMontoNoPositivoSinCambiarSaldoNiAuditar() {
        assertThatThrownBy(() -> service.recargarCreditos(owner, 1L, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(owner.getSaldo()).isEqualTo(10.0);
        verify(auditorias, never()).crear(any());
        verify(usuarios, never()).crearUsuario(any());
    }

    @Test
    void rechazaRecargaDeOtraCuenta() {
        assertThatThrownBy(() -> service.recargarCreditos(other, 1L, BigDecimal.ONE))
                .isInstanceOf(AccessDeniedException.class);
        verify(auditorias, never()).crear(any());
    }
}
