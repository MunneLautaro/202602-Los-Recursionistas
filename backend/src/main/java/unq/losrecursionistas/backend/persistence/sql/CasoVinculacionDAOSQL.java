package unq.losrecursionistas.backend.persistence.sql;

import org.springframework.data.jpa.repository.JpaRepository;
import unq.losrecursionistas.backend.model.CasoVinculacion;

public interface CasoVinculacionDAOSQL extends JpaRepository<CasoVinculacion, Long> {
}
