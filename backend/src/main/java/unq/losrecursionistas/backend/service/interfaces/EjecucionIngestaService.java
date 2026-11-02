package unq.losrecursionistas.backend.service.interfaces;

import unq.losrecursionistas.backend.model.EjecucionIngesta;

public interface EjecucionIngestaService {
    EjecucionIngesta crearEjecucion(EjecucionIngesta.Alcance alcance, Long alcanceId);
    EjecucionIngesta ultima();
}
