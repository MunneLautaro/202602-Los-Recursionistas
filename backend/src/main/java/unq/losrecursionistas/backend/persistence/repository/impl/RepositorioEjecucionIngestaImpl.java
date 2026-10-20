package unq.losrecursionistas.backend.persistence.repository.impl;

import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.model.EjecucionIngesta;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEjecucionIngesta;
import unq.losrecursionistas.backend.persistence.sql.EjecucionIngestaDAOSQL;

@Component
public class RepositorioEjecucionIngestaImpl implements RepositorioEjecucionIngesta {
    private final EjecucionIngestaDAOSQL dao;

    public RepositorioEjecucionIngestaImpl(EjecucionIngestaDAOSQL dao) {
        this.dao = dao;
    }

    @Override
    public EjecucionIngesta guardar(EjecucionIngesta ejecucion) {
        return dao.save(ejecucion);
    }

    @Override
    public boolean existeActiva() {
        return dao.findFirstByEstadoOrderByInicioDesc(EjecucionIngesta.Estado.ACTIVA).isPresent();
    }

    @Override
    public synchronized EjecucionIngesta reservar(EjecucionIngesta ejecucion) {
        if (existeActiva()) {
            ejecucion.setEstado(EjecucionIngesta.Estado.RECHAZADA);
            ejecucion.setMotivoRechazo("Ya existe una ejecucion activa");
            ejecucion.setFin(java.time.OffsetDateTime.now());
        }
        return dao.save(ejecucion);
    }

    @Override
    public EjecucionIngesta ultima() {
        return dao.findFirstByOrderByInicioDesc().orElse(null);
    }
}
