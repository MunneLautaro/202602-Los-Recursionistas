package unq.losrecursionistas.backend.controller.dto.whoscored;

import unq.losrecursionistas.backend.model.EjecucionIngesta;

public record IniciarIngestaResponse(Long ejecucionId, EjecucionIngesta.Estado estado,
                                     EjecucionIngesta.Alcance alcance) {
    public static IniciarIngestaResponse from(EjecucionIngesta ejecucion) {
        return new IniciarIngestaResponse(ejecucion.getId(), ejecucion.getEstado(), ejecucion.getAlcance());
    }
}
