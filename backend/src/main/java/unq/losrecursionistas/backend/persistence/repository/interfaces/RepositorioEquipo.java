package unq.losrecursionistas.backend.persistence.repository.interfaces;

import unq.losrecursionistas.backend.model.Equipo;

public interface RepositorioEquipo {
    Equipo guardar(Equipo equipo);
    Equipo buscarPorIdExterno(Long idExterno, Equipo equipo);
    Equipo guardarOActualizar(Equipo equipo);
}
