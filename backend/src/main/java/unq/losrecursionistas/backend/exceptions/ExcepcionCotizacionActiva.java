package unq.losrecursionistas.backend.exceptions;

public class ExcepcionCotizacionActiva extends RuntimeException {
    public ExcepcionCotizacionActiva() {
        super("Ya existe un recálculo de cotizaciones en curso");
    }
}
