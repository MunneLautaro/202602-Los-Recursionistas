package unq.losrecursionistas.backend.services.interfaces;

import unq.losrecursionistas.backend.model.ApiKey;
import unq.losrecursionistas.backend.model.Usuario;

public interface UsuarioService {
	ApiKey crear(String username);

	Usuario autenticar(String username, String apiKey);
}
