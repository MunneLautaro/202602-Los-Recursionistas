package unq.losrecursionistas.backend.persistence.sql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unq.losrecursionistas.backend.model.Liga;

import java.util.Optional;

@Repository
public interface LigaDAOSQL extends JpaRepository<Liga, Long> {
    Optional<Liga> findByCodigo(String codigo);
    Optional<Liga> findByIdExterno(Long idExterno);
}
