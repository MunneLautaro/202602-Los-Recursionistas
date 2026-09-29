package unq.losrecursionistas.backend.service.interfaces;

import unq.losrecursionistas.backend.model.Equipo;

import java.util.List;

public interface EquipoService {
    Equipo guardar(Equipo equipo);
    Equipo buscarPorIdExterno(Long idExterno, Equipo equipo);
    Equipo guardarOActualizar(Equipo equipo);

    List<Equipo> obtenerTodosLosEquiposDeLiga(Long ligaId);
}
