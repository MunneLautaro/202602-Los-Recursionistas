package unq.losrecursionistas.backend.exceptions;

public class CredencialesInvalidasException extends DomainException {

	public CredencialesInvalidasException() {
		super("CREDENCIALES_INVALIDAS", "Las credenciales no son validas");
	}
}
