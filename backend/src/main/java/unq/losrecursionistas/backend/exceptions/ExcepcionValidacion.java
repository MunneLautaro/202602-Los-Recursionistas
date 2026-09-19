package unq.losrecursionistas.backend.exceptions;

public class ExcepcionValidacion extends RuntimeException {

	public ExcepcionValidacion(String mensaje) {
		super(mensaje);
	}

	public ExcepcionValidacion(String mensaje, Throwable causa) {
		super(mensaje, causa);
	}
}