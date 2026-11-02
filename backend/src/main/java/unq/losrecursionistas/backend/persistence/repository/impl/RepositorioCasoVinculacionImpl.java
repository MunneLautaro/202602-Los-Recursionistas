package unq.losrecursionistas.backend.persistence.repository.impl;

import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.model.CasoVinculacion;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioCasoVinculacion;
import unq.losrecursionistas.backend.persistence.sql.CasoVinculacionDAOSQL;

@Component
public class RepositorioCasoVinculacionImpl implements RepositorioCasoVinculacion {
    private final CasoVinculacionDAOSQL dao;

    public RepositorioCasoVinculacionImpl(CasoVinculacionDAOSQL dao) {
        this.dao = dao;
    }

    @Override
    public CasoVinculacion guardar(CasoVinculacion caso) {
        return dao.save(caso);
    }
}
