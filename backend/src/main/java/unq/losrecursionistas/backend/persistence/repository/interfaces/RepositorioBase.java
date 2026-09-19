package unq.losrecursionistas.backend.persistence.repository.interfaces;

import java.util.Optional;

public interface RepositorioBase<T, ID> {

	Optional<T> buscarPorId(ID id);

	T guardar(T entidad);
}