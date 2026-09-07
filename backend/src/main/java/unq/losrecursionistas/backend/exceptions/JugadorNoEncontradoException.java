package unq.losrecursionistas.backend.exceptions;

public class JugadorNoEncontradoException extends DomainException {

	public JugadorNoEncontradoException(Long id) {
		super("RECURSO_NO_ENCONTRADO", "El jugador no existe: " + id);
	}
}
