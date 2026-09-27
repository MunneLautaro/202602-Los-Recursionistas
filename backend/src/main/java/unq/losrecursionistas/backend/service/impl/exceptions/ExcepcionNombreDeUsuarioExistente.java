package unq.losrecursionistas.backend.service.impl.exceptions;

public class ExcepcionNombreDeUsuarioExistente extends RuntimeException {
  public ExcepcionNombreDeUsuarioExistente(String message) {
    super(message);
  }
}
