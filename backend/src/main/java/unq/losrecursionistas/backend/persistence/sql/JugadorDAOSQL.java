package unq.losrecursionistas.backend.persistence.sql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import unq.losrecursionistas.backend.model.Jugador;

import java.util.Optional;

@Repository
public interface JugadorDAOSQL extends JpaRepository<Jugador, Long>, JpaSpecificationExecutor<Jugador> {
    Optional<Jugador> findByIdExterno(Long idExterno);
}


