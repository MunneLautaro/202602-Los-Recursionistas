package unq.losrecursionistas.backend.exceptions;

public class UsuarioNoEncontradoException extends DomainException {

    public UsuarioNoEncontradoException(String username) {
        super("RECURSO_NO_ENCONTRADO", "El usuario no existe: " + username);
    }
}
