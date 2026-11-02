package unq.losrecursionistas.backend.controller.dto.whoscored;

import unq.losrecursionistas.backend.model.EjecucionIngesta;

public record UltimaEjecucionResponse(boolean disponible, EjecucionWhoscoredResponse ejecucion) {
    public static UltimaEjecucionResponse from(EjecucionIngesta ejecucion) {
        return ejecucion == null
                ? new UltimaEjecucionResponse(false, null)
                : new UltimaEjecucionResponse(true, EjecucionWhoscoredResponse.from(ejecucion));
    }
}
