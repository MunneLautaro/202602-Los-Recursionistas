package unq.losrecursionistas.backend.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import unq.losrecursionistas.backend.model.*;
import java.math.BigDecimal;

public interface MercadoService {
    Orden crearOrden(Usuario usuario, Long jugadorId, int cantidad, TipoOrden tipo);
    Page<Orden> listarOrdenes(Long jugadorId, TipoOrden tipo, Pageable pageable);
    Page<Orden> listarOrdenes(Long jugadorId, TipoOrden tipo, String jugadorNombre, Pageable pageable);
    Page<Orden> listarOrdenesDeUsuario(Long usuarioId, Pageable pageable);
    Page<TransaccionMercado> listarTransacciones(Long usuarioId, Pageable pageable);
    Page<TenenciaToken> listarTenencias(Long usuarioId, Pageable pageable);
    Usuario recargarCreditos(Usuario solicitante, Long usuarioId, BigDecimal monto);
}
