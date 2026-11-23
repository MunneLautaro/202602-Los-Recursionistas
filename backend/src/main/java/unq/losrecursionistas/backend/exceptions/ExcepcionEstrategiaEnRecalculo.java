package unq.losrecursionistas.backend.exceptions;

public class ExcepcionEstrategiaEnRecalculo extends RuntimeException {
    public ExcepcionEstrategiaEnRecalculo() {
        super("No se puede cambiar la estrategia mientras hay un recalculo en curso");
    }
}
