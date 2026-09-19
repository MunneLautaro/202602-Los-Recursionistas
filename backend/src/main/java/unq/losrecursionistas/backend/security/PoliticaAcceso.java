package unq.losrecursionistas.backend.security;

public interface PoliticaAcceso {

	boolean puedeAcceder(String sujeto, String recurso);
}