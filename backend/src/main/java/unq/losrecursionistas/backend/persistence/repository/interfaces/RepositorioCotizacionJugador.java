package unq.losrecursionistas.backend.persistence.repository.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import unq.losrecursionistas.backend.model.CotizacionJugador;

public interface RepositorioCotizacionJugador extends RepositorioBase<CotizacionJugador, Long> {

	Page<CotizacionJugador> buscarPorJugadorId(Long jugadorId, Pageable pageable);
}
