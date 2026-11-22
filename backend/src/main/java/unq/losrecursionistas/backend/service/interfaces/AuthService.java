package unq.losrecursionistas.backend.service.interfaces;

import unq.losrecursionistas.backend.model.Usuario;

public interface AuthService {

	Usuario registrarUsuario(Usuario usuario);

	ResultadoLogin login(String nombreUsuario, String contrasena);
}

