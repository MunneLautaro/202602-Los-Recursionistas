package unq.losrecursionistas.backend.persistence.repository.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import unq.losrecursionistas.backend.model.Equipo;

public interface RepositorioEquipo extends RepositorioBase<Equipo, Long> {

	Page<Equipo> buscarPorLigaId(Long ligaId, Pageable pageable);
}
