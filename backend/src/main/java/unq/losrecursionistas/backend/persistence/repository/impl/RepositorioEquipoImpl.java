package unq.losrecursionistas.backend.persistence.repository.impl;

import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.model.Equipo;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEquipo;
import unq.losrecursionistas.backend.persistence.sql.EquipoDAOSQL;

import java.util.List;
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

    @Override
    public Equipo guardarOActualizar(Equipo equipo) {
        if (equipo.getIdExterno() != null) {
            Optional<Equipo> existente = equipoDAOSQL.findByIdExterno(equipo.getIdExterno());
            if (existente.isPresent()) {
                Equipo e = existente.get();
                e.setNombre(equipo.getNombre());
                e.setNombreCorto(equipo.getNombreCorto());
                e.setSigla(equipo.getSigla());
                e.setEscudoUrl(equipo.getEscudoUrl());
                e.setColores(equipo.getColores());
                e.setEstadio(equipo.getEstadio());
                e.setFundacion(equipo.getFundacion());
                if (equipo.getLiga() != null) {
                    e.setLiga(equipo.getLiga());
                }
                return equipoDAOSQL.save(e);
            }
        }
        return equipoDAOSQL.save(equipo);
    }

    @Override
    public List<Equipo> obtenerEquiposDeLiga(Long ligaId) {
        return equipoDAOSQL.findByLigaId(ligaId);
    }
}
