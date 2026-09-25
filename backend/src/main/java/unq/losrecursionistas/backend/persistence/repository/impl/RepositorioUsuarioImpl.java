package unq.losrecursionistas.backend.persistence.repository.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioUsuario;
import unq.losrecursionistas.backend.persistence.sql.UsuarioDAOSQL;

@Component
public class RepositorioUsuarioImpl implements RepositorioUsuario {

	private final UsuarioDAOSQL usuarioDAOSQL;

	public RepositorioUsuarioImpl(UsuarioDAOSQL usuarioDAOSQL) {
		this.usuarioDAOSQL = usuarioDAOSQL;
	}

	@Override
	public Optional<Usuario> buscarPorId(Long id) {
		return usuarioDAOSQL.findById(id);
	}

	@Override
	public Usuario guardar(Usuario usuario) {
		return usuarioDAOSQL.save(usuario);
	}

	@Override
	public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) {
		return usuarioDAOSQL.findByNombreUsuario(nombreUsuario);
	}

	@Override
	public Page<Usuario> buscarTodos(Pageable pageable) {
		return usuarioDAOSQL.findAll(pageable);
	}
}