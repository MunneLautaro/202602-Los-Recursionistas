package unq.losrecursionistas.backend.security;

import java.util.Optional;

public interface ManejadorToken {

	String crearToken(String sujeto);

	Optional<String> extraerSujeto(String token);
}