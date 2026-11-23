package unq.losrecursionistas.backend.persistence.repository.impl;

import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.model.ConfiguracionEstrategia;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioConfiguracionEstrategia;
import unq.losrecursionistas.backend.persistence.sql.ConfiguracionEstrategiaDAOSQL;

@Component
public class RepositorioConfiguracionEstrategiaImpl implements RepositorioConfiguracionEstrategia {
    private final ConfiguracionEstrategiaDAOSQL dao;

    public RepositorioConfiguracionEstrategiaImpl(ConfiguracionEstrategiaDAOSQL dao) {
        this.dao = dao;
    }

    @Override
    public ConfiguracionEstrategia buscar() {
        return dao.findById(1L).orElse(null);
    }

    @Override
    public ConfiguracionEstrategia guardar(ConfiguracionEstrategia configuracion) {
        return dao.save(configuracion);
    }
}
