package unq.losrecursionistas.backend.exceptions;

public class ExcepcionCotizacionNoEncontrada extends ExcepcionDominio {
    public ExcepcionCotizacionNoEncontrada() {
        super("No existe una cotizacion vigente para el jugador");
    }
}
