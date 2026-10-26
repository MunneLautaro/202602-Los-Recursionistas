package unq.losrecursionistas.backend.persistence.repository.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import unq.losrecursionistas.backend.model.TransaccionMercado;

public interface RepositorioTransaccionMercado {
    TransaccionMercado crear(TransaccionMercado transaccion);
    Page<TransaccionMercado> buscarPorUsuario(Long usuarioId, Pageable pageable);
}
