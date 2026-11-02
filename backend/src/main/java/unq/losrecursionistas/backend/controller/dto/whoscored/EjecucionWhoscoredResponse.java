package unq.losrecursionistas.backend.controller.dto.whoscored;

import unq.losrecursionistas.backend.model.EjecucionIngesta;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;

public record EjecucionWhoscoredResponse(
        Long id, EjecucionIngesta.Estado estado, EjecucionIngesta.Alcance alcance, Long idAlcance,
        OffsetDateTime inicio, OffsetDateTime fin, LocalDate temporadaInicio, LocalDate temporadaFin,
        Contadores contadores, int errores, int anomalias, String motivoRechazo, Map<String, Object> detalle) {
    public static EjecucionWhoscoredResponse from(EjecucionIngesta e) {
        return new EjecucionWhoscoredResponse(e.getId(), e.getEstado(), e.getAlcance(), e.getIdAlcance(),
                e.getInicio(), e.getFin(), e.getTemporadaInicio(), e.getTemporadaFin(),
                new Contadores(e.getJugadoresProcesados(), e.getJugadoresVinculados(),
                        e.getJugadoresNoVinculados(), e.getPartidosInsertados(), e.getPartidosActualizados()),
                e.getErrores(), e.getAnomalias(), e.getMotivoRechazo(), e.getDetalle());
    }

    public record Contadores(int jugadoresProcesados, int jugadoresVinculados, int jugadoresNoVinculados,
                             int partidosInsertados, int partidosActualizados) {}
}
