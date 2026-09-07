package unq.losrecursionistas.backend.persistence.sql;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import unq.losrecursionistas.backend.model.Usuario;

public interface UsuarioDAOSQL extends JpaRepository<Usuario, Long> {

    boolean existsByUsername(String username);

    Optional<Usuario> findByUsername(String username);
}
