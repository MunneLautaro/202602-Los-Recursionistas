package unq.losrecursionistas.backend.service.interfaces;

import unq.losrecursionistas.backend.model.Jugador;

import java.util.List;

public interface JugadorService {
    void guardarTodos(List<Jugador> jugadores);

    Jugador obtenerJugadorPorId(Long id);
}
