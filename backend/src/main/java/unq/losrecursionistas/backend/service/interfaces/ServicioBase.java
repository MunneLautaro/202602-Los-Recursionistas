package unq.losrecursionistas.backend.service.interfaces;

public interface ServicioBase<T> {

	T guardar(T entidad);
}