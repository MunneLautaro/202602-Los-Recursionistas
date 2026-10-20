package unq.losrecursionistas.backend.persistence.repository.interfaces;

import unq.losrecursionistas.backend.model.EjecucionIngesta;

public interface RepositorioEjecucionIngesta {
    EjecucionIngesta guardar(EjecucionIngesta ejecucion);
    boolean existeActiva();
    EjecucionIngesta reservar(EjecucionIngesta ejecucion);
    EjecucionIngesta ultima();
}
