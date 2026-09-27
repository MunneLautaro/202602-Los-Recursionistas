package unq.losrecursionistas.backend.persistence.sql;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import unq.losrecursionistas.backend.model.Usuario;

@Repository
public interface UsuarioDAOSQL extends JpaRepository<Usuario, Long> {

	Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    @Query(value = "SELECT COUNT(*) > 0 FROM usuarios WHERE nombre_usuario = :nombreUsuario", nativeQuery = true)
    boolean existsByNombreUsuario(String nombreUsuario);
}