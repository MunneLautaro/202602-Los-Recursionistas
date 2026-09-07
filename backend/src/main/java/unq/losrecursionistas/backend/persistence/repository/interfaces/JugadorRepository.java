package unq.losrecursionistas.backend.persistence.repository.interfaces;

import java.util.List;
import java.util.Optional;

import unq.losrecursionistas.backend.model.Jugador;

public interface JugadorRepository {
	List<Jugador> findAll();
	Optional<Jugador> findById(Long id);
}