package unq.losrecursionistas.backend.persistence.repository.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import unq.losrecursionistas.backend.model.Jugador;

public interface RepositorioJugador extends RepositorioBase<Jugador, Long> {

	Page<Jugador> buscarPorEquipoId(Long equipoId, Pageable pageable);

	Page<Jugador> buscarPorLigaId(Long ligaId, Pageable pageable);

	Page<Jugador> buscarPorActivo(Boolean activo, Pageable pageable);
}
