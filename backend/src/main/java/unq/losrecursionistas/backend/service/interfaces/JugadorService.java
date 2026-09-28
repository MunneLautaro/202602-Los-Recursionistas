package unq.losrecursionistas.backend.service.interfaces;

import org.springframework.data.domain.Page;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.model.JugadorFiltro;

import java.util.List;

public interface JugadorService {
    void guardarTodos(List<Jugador> jugadores);

    Jugador obtenerJugadorPorId(Long id);

    Page<Jugador> buscarJugadoresConFiltro(JugadorFiltro filtro, int page);
}
