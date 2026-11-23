package unq.losrecursionistas.backend.persistence.repository.interfaces;

import unq.losrecursionistas.backend.model.CambioEstrategia;

public interface RepositorioCambioEstrategia {
    CambioEstrategia guardar(CambioEstrategia cambio);
    CambioEstrategia ultimo();
}
