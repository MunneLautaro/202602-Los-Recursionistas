package unq.losrecursionistas.backend.service.interfaces;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface JwtService {

	String generarToken(UserDetails usuario);

	Optional<String> extraerUsername(String token);

	boolean tokenValido(String token, UserDetails usuario);
}