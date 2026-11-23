package unq.losrecursionistas.backend.persistence.repository.impl;

import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.model.CambioEstrategia;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioCambioEstrategia;
import unq.losrecursionistas.backend.persistence.sql.CambioEstrategiaDAOSQL;

@Component
public class RepositorioCambioEstrategiaImpl implements RepositorioCambioEstrategia {
    private final CambioEstrategiaDAOSQL dao;

    public RepositorioCambioEstrategiaImpl(CambioEstrategiaDAOSQL dao) {
        this.dao = dao;
    }

    @Override
    public CambioEstrategia guardar(CambioEstrategia cambio) {
        return dao.save(cambio);
    }

    @Override
    public CambioEstrategia ultimo() {
        return dao.findFirstByOrderByTimestampCambioDescIdDesc().orElse(null);
    }
}
