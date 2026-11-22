package unq.losrecursionistas.backend.persistence.repository.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import unq.losrecursionistas.backend.model.Cotizacion;
import unq.losrecursionistas.backend.persistence.sql.CotizacionDAOSQL;

public interface RepositorioCotizacion {
    Cotizacion guardar(Cotizacion cotizacion);
    Cotizacion vigente(Long jugadorId);
    Page<Cotizacion> buscarPorJugador(Long jugadorId, java.time.LocalDate desde,
                                      java.time.LocalDate hasta, Pageable pageable);
    /**
     * Devuelve las ultimas {@code limite} cotizaciones del jugador dentro del rango,
     * ordenadas cronologicamente (de la mas antigua a la mas reciente).
     */
    java.util.List<Cotizacion> buscarSeriePorJugador(Long jugadorId, java.time.LocalDate desde,
                                                     java.time.LocalDate hasta, int limite);
    Page<CotizacionDAOSQL.RankingProyeccion> buscarRanking(Long ligaId, String posicion,
                                                            Pageable pageable);
}
