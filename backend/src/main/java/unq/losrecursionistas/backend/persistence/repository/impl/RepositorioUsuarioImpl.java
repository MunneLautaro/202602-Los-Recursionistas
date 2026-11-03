package unq.losrecursionistas.backend.persistence.repository.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.exceptions.ExcepcionRecursoNoEncontrado;
import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioUsuario;
import unq.losrecursionistas.backend.persistence.sql.UsuarioDAOSQL;

@Component
public class RepositorioUsuarioImpl implements RepositorioUsuario {

	private static final String USUARIO_NO_ENCONTRADO = "Usuario no encontrado";

	private final UsuarioDAOSQL usuarioDAOSQL;

	public RepositorioUsuarioImpl(UsuarioDAOSQL usuarioDAOSQL) {
		this.usuarioDAOSQL = usuarioDAOSQL;
	}


	@Override
	public Usuario buscarPorNombreUsuario(String nombreUsuario) {
		return usuarioDAOSQL.findByNombreUsuario(nombreUsuario)
				.orElseThrow(() -> new ExcepcionRecursoNoEncontrado(USUARIO_NO_ENCONTRADO));
	}

	@Override
	public Usuario buscarPorId(Long id) {
		return usuarioDAOSQL.findById(id)
				.orElseThrow(() -> new ExcepcionRecursoNoEncontrado(USUARIO_NO_ENCONTRADO));
	}

	@Override
	public Usuario buscarPorIdBloqueado(Long id) {
		return usuarioDAOSQL.findLockedById(id)
				.orElseThrow(() -> new ExcepcionRecursoNoEncontrado(USUARIO_NO_ENCONTRADO));
	}

	@Override
	public Usuario buscarCustodio() {
		return usuarioDAOSQL.findFirstByAutoridadesContainingOrderByIdAsc("ROLE_ADMIN")
				.orElseThrow(() -> new RuntimeException("No existe un usuario custodio"));
	}

	@Override
	public Usuario crearUsuario(Usuario usuario) {
		return usuarioDAOSQL.save(usuario);
	}

	@Override
	public boolean existeElUsuario(String nombreUsuario) {
		return usuarioDAOSQL.existsByNombreUsuario(nombreUsuario);
	}

	@Override
	public Page<Usuario> buscarTodos(Pageable pageable) {
		return usuarioDAOSQL.findAll(pageable);
	}
}