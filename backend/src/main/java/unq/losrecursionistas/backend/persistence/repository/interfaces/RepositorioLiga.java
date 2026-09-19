package unq.losrecursionistas.backend.persistence.repository.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import unq.losrecursionistas.backend.model.Liga;

public interface RepositorioLiga extends RepositorioBase<Liga, Long> {

	Page<Liga> buscarTodos(Pageable pageable);
}
