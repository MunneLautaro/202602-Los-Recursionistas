package unq.losrecursionistas.backend.persistence.repository.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

import unq.losrecursionistas.backend.model.Usuario;

public interface RepositorioUsuario extends RepositorioBase<Usuario, Long> {

	Page<Usuario> buscarTodos(Pageable pageable);

	Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario);
}
