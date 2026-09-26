package unq.losrecursionistas.backend.persistence.repository.impl;

import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.model.Equipo;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEquipo;
import unq.losrecursionistas.backend.persistence.sql.EquipoDAOSQL;

import java.util.Optional;

@Component
public class RepositorioEquipoImpl implements RepositorioEquipo {

    private final EquipoDAOSQL equipoDAOSQL;

    public RepositorioEquipoImpl(EquipoDAOSQL equipoDAOSQL) {
        this.equipoDAOSQL = equipoDAOSQL;
    }

    @Override
    public Equipo guardar(Equipo equipo) {
        return equipoDAOSQL.save(equipo);
    }

    @Override
    public Equipo buscarPorIdExterno(Long idExterno, Equipo equipo) {
        return equipoDAOSQL.findByIdExterno(idExterno).orElseGet(() -> equipoDAOSQL.save(equipo));
    }
}
