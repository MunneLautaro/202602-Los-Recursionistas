package unq.losrecursionistas.backend.service.interfaces;

import unq.losrecursionistas.backend.model.Equipo;

public interface EquipoService {
    Equipo guardar(Equipo equipo);
    Equipo buscarPorIdExterno(Long idExterno, Equipo equipo);
    Equipo guardarOActualizar(Equipo equipo);
}
