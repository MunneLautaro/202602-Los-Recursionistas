package unq.losrecursionistas.backend.persistence.repository.interfaces;

import unq.losrecursionistas.backend.model.Usuario;

public interface UsuarioRepository {
	boolean estaRegistradoElUsername(String username);
	Usuario guardar(Usuario usuario);
	Usuario recuperarPorUsername(String username);
}