package unq.losrecursionistas.backend.persistence.sql;

import org.springframework.data.jpa.repository.JpaRepository;
import unq.losrecursionistas.backend.model.EjecucionIngesta;

import java.util.Optional;

public interface EjecucionIngestaDAOSQL extends JpaRepository<EjecucionIngesta, Long> {
    Optional<EjecucionIngesta> findFirstByEstadoOrderByInicioDesc(EjecucionIngesta.Estado estado);
}
