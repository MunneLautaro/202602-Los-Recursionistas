package unq.losrecursionistas.backend.persistence.repository.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import unq.losrecursionistas.backend.model.Usuario;

public interface RepositorioUsuario{

	Page<Usuario> buscarTodos(Pageable pageable);

	Usuario buscarPorNombreUsuario(String nombreUsuario);
	Usuario buscarPorIdBloqueado(Long id);
	Usuario buscarCustodio();

	Usuario crearUsuario(Usuario usuario);

	boolean existeElUsuario(String nombreUsuario);
}
