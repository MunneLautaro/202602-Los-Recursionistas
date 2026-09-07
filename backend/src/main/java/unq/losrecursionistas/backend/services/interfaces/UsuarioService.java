package unq.losrecursionistas.backend.services.interfaces;

import unq.losrecursionistas.backend.controller.dto.AltaUsuarioResponse;
import unq.losrecursionistas.backend.controller.dto.TokenResponse;

public interface UsuarioService {
	AltaUsuarioResponse crear(String username);

	TokenResponse emitirToken(String username, String apiKey);
}
