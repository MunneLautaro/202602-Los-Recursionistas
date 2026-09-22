package unq.losrecursionistas.backend.service.interfaces;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

	String generarToken(UserDetails usuario);

	Optional<String> extraerUsername(String token);

	boolean tokenValido(String token, UserDetails usuario);
}