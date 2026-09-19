package unq.losrecursionistas.backend.persistence.repository.impl;

import java.util.Optional;

import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioBase;

public abstract class RepositorioBaseImpl<T, ID> implements RepositorioBase<T, ID> {

	@Override
	public abstract Optional<T> buscarPorId(ID id);

	@Override
	public abstract T guardar(T entidad);
}