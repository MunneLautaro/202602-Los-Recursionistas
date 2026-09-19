package unq.losrecursionistas.backend.persistence.repository.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import unq.losrecursionistas.backend.model.PosicionPortfolio;

public interface RepositorioPosicionPortfolio extends RepositorioBase<PosicionPortfolio, Long> {

	Page<PosicionPortfolio> buscarPorUsuarioId(Long usuarioId, Pageable pageable);
}
