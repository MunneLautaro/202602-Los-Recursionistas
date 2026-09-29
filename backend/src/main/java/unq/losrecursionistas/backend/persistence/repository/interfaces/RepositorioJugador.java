package unq.losrecursionistas.backend.persistence.repository.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.model.JugadorFiltro;

import java.util.List;

public interface RepositorioJugador {

    void guardarTodos(List<Jugador> jugadores);

    Jugador obtenerJugadorPorId(Long id);

    Page<Jugador> buscarJugadoresConFiltro(JugadorFiltro filtro, Pageable pageable);
}
