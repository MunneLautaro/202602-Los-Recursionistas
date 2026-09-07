package unq.losrecursionistas.backend.persistence.repository.impl;

import org.springframework.stereotype.Repository;

import unq.losrecursionistas.backend.exceptions.UsuarioNoEncontradoException;
import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.UsuarioRepository;
import unq.losrecursionistas.backend.persistence.sql.UsuarioDAOSQL;

@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {

	private final UsuarioDAOSQL usuarioDAOSQL;

	public UsuarioRepositoryImpl(UsuarioDAOSQL usuarioDAOSQL) {
		this.usuarioDAOSQL = usuarioDAOSQL;
	}

	@Override
	public boolean estaRegistradoElUsername(String username) {
		return usuarioDAOSQL.existsByUsername(username);
	}

	@Override
	public Usuario guardar(Usuario usuario) {
		return usuarioDAOSQL.save(usuario);
	}

	@Override
	public Usuario recuperarPorUsername(String username) {
		return usuarioDAOSQL.findByUsername(username)
				.orElseThrow(() -> new UsuarioNoEncontradoException(username));
	}
}