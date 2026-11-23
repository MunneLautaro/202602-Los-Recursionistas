package unq.losrecursionistas.backend.persistence.sql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unq.losrecursionistas.backend.model.CambioEstrategia;

import java.util.Optional;

@Repository
public interface CambioEstrategiaDAOSQL extends JpaRepository<CambioEstrategia, Long> {
    Optional<CambioEstrategia> findFirstByOrderByTimestampCambioDescIdDesc();
}
