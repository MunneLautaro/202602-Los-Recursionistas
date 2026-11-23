package unq.losrecursionistas.backend.persistence.repository.interfaces;

import unq.losrecursionistas.backend.model.ConfiguracionEstrategia;

public interface RepositorioConfiguracionEstrategia {
    ConfiguracionEstrategia buscar();
    ConfiguracionEstrategia guardar(ConfiguracionEstrategia configuracion);
}
