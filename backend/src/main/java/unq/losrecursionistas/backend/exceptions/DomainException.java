package unq.losrecursionistas.backend.exceptions;

import unq.losrecursionistas.backend.exceptions.businessException.BusinessException;

/** Compatibility base for existing domain exception names. */
@Deprecated
public class DomainException extends BusinessException {

	public DomainException(String codigo, String mensaje) {
		super(codigo, mensaje);
	}
}