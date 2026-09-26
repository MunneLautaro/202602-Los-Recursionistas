package unq.losrecursionistas.backend.service.interfaces;

import unq.losrecursionistas.backend.model.Equipo;

import java.util.Optional;

public interface EquipoService {
    Equipo guardar(Equipo equipo);
    Equipo buscarPorIdExterno(Long idExterno, Equipo equipo);
}
