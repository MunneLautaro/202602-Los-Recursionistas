package unq.losrecursionistas.backend.persistence.repository.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import unq.losrecursionistas.backend.model.Cotizacion;
import unq.losrecursionistas.backend.persistence.sql.CotizacionDAOSQL;

public interface RepositorioCotizacion {
    Cotizacion guardar(Cotizacion cotizacion);
    Page<Cotizacion> buscarPorJugador(Long jugadorId, java.time.LocalDate desde,
                                      java.time.LocalDate hasta, Pageable pageable);
    Page<CotizacionDAOSQL.RankingProyeccion> buscarRanking(Long ligaId, String posicion,
                                                            Pageable pageable);
}
