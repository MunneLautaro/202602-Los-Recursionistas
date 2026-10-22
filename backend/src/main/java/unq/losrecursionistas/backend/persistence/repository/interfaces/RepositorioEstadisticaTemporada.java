package unq.losrecursionistas.backend.persistence.repository.interfaces;

import unq.losrecursionistas.backend.model.EstadisticaTemporada;

import java.time.LocalDate;
import java.util.List;

public interface RepositorioEstadisticaTemporada {
    List<EstadisticaTemporada> buscarPorJugadorYTemporada(Long jugadorId, LocalDate temporadaInicio);
    List<EstadisticaTemporada> buscarRanking(String liga, String posicion, LocalDate temporadaInicio, int limit);
    void reemplazarPorJugadorYTemporada(Long jugadorId, LocalDate temporadaInicio, List<EstadisticaTemporada> estadisticas);
}
