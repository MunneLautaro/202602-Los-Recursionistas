package unq.losrecursionistas.backend.persistence.sql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unq.losrecursionistas.backend.model.ConfiguracionEstrategia;

@Repository
public interface ConfiguracionEstrategiaDAOSQL extends JpaRepository<ConfiguracionEstrategia, Long> {
}
