package unq.losrecursionistas.backend.persistence.repository.impl;

import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.model.AuditoriaTransaccional;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioAuditoriaTransaccional;
import unq.losrecursionistas.backend.persistence.sql.AuditoriaTransaccionalDAOSQL;

@Component
public class RepositorioAuditoriaTransaccionalImpl implements RepositorioAuditoriaTransaccional {
    private final AuditoriaTransaccionalDAOSQL dao;
    public RepositorioAuditoriaTransaccionalImpl(AuditoriaTransaccionalDAOSQL dao) { this.dao = dao; }
    public AuditoriaTransaccional crear(AuditoriaTransaccional auditoria) { return dao.save(auditoria); }
}
