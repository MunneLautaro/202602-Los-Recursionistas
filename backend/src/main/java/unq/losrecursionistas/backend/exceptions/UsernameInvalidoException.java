package unq.losrecursionistas.backend.exceptions;

public class UsernameInvalidoException extends DomainException {

	public UsernameInvalidoException() {
		super("VALIDACION_INVALIDA", "El username es obligatorio");
	}
}
