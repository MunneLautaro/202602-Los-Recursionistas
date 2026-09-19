package unq.losrecursionistas.backend.persistence.repository.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import unq.losrecursionistas.backend.model.TransaccionAuditoria;

public interface RepositorioTransaccionAuditoria extends RepositorioBase<TransaccionAuditoria, Long> {

	Page<TransaccionAuditoria> buscarPorUsuarioId(Long usuarioId, Pageable pageable);
}
