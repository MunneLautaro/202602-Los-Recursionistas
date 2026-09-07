package unq.losrecursionistas.backend.persistence.repository.impl;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.UsuarioRepository;

@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {

	private final Map<String, Usuario> usuarios = new ConcurrentHashMap<>();

	@Override
	public boolean existsByUsername(String username) { return usuarios.containsKey(username); }

	@Override
	public Usuario save(Usuario usuario) {
		usuarios.put(usuario.username(), usuario);
		return usuario;
	}

	@Override
	public Optional<Usuario> findByUsername(String username) { return Optional.ofNullable(usuarios.get(username)); }
}