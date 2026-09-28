package unq.losrecursionistas.backend.persistence.sql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unq.losrecursionistas.backend.model.Equipo;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipoDAOSQL extends JpaRepository<Equipo, Long> {
    Optional<Equipo> findByIdExterno(Long idExterno);

    List<Equipo> findByLigaId(Long ligaId);
}
