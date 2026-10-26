package unq.losrecursionistas.backend.persistence.sql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unq.losrecursionistas.backend.model.AuditoriaTransaccional;

@Repository
public interface AuditoriaTransaccionalDAOSQL extends JpaRepository<AuditoriaTransaccional, Long> {
}
