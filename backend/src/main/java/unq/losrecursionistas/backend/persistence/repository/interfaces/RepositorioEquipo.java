package unq.losrecursionistas.backend.persistence.repository.interfaces;

import unq.losrecursionistas.backend.model.Equipo;

import java.util.List;

public interface RepositorioEquipo {
    Equipo guardar(Equipo equipo);
    Equipo buscarPorIdExterno(Long idExterno, Equipo equipo);
    Equipo buscarPorIdWhoscored(Long idWhoscored);
    Equipo guardarOActualizar(Equipo equipo);

    List<Equipo> obtenerEquiposDeLiga(Long ligaId);
    List<Equipo> obtenerTodos();
}
