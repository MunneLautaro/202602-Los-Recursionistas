package unq.losrecursionistas.backend.persistence.repository.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import unq.losrecursionistas.backend.model.*;
import java.math.BigDecimal;
import java.util.List;

public interface RepositorioOrden {
    Orden guardar(Orden orden);
    List<Orden> buscarCompatiblesBloqueadas(Long jugadorId, TipoOrden tipo,
                                             BigDecimal precio, Long usuarioId);
    Page<Orden> buscarActivas(Long jugadorId, TipoOrden tipo, Pageable pageable);
    Orden buscarLiquidezInicialBloqueada(Long usuarioId, Long jugadorId);
}
