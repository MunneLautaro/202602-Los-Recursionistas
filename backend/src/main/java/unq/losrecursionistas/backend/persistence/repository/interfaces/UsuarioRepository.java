package unq.losrecursionistas.backend.persistence.repository.interfaces;

import java.util.Optional;

import unq.losrecursionistas.backend.model.Usuario;

public interface UsuarioRepository {
	boolean existsByUsername(String username);
	Usuario save(Usuario usuario);
	Optional<Usuario> findByUsername(String username);
}