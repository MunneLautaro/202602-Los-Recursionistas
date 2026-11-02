package unq.losrecursionistas.backend.service.interfaces;

import unq.losrecursionistas.backend.model.Orden;

public interface MatchingEngine {
    void ejecutar(Orden orden);
}
