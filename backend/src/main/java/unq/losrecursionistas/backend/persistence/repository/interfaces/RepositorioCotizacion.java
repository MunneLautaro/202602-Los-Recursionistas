package unq.losrecursionistas.backend.persistence.repository.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import unq.losrecursionistas.backend.model.Cotizacion;
import unq.losrecursionistas.backend.model.RankingCotizacion;

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
    Page<RankingCotizacion> buscarRanking(Long ligaId, String posicion, Pageable pageable);
    /**
     * Cotizaciones calculadas por dia y variacion promedio respecto del valor previo,
     * solo los dias con datos. El rango esta acotado a 90 dias.
     */
    java.util.List<unq.losrecursionistas.backend.model.ActividadDiariaCotizacion> resumirPorDia(
            java.time.LocalDate desde, java.time.LocalDate hasta);
}
