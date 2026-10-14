package unq.losrecursionistas.backend.service.interfaces;

import unq.losrecursionistas.backend.model.EjecucionIngesta;

public interface IngestaWhoscoredService {
    EjecucionIngesta iniciar(EjecucionIngesta.Alcance alcance, Long alcanceId);
}
