package unq.losrecursionistas.backend.exceptions;

public class ParametrosPaginacionInvalidosException extends DomainException {

	public ParametrosPaginacionInvalidosException() {
		super("PARAMETROS_INVALIDOS", "La paginacion no es valida");
	}
}
