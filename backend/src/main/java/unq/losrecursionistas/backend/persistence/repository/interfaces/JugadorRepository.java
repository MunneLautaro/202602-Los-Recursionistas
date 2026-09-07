package unq.losrecursionistas.backend.persistence.repository.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import unq.losrecursionistas.backend.model.Jugador;

public interface JugadorRepository {
	Page<Jugador> recuperarJugadores(String liga, String equipo, String posicion, Boolean activo, Pageable pageable);
	Jugador recuperarPorId(Long id);
}