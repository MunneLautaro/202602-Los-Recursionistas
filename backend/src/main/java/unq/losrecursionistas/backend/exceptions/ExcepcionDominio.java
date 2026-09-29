package unq.losrecursionistas.backend.exceptions;

public class ExcepcionDominio extends RuntimeException {

	public ExcepcionDominio(String mensaje) {
		super(mensaje);
	}

	public ExcepcionDominio(String mensaje, Throwable causa) {
		super(mensaje, causa);
	}
}