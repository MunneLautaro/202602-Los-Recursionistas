package unq.losrecursionistas.backend.exceptions;

public class UsuarioYaExisteException extends DomainException {

	public UsuarioYaExisteException(String username) {
		super("CONFLICTO", "El username ya existe: " + username);
	}
}
