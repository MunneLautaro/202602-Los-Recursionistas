package unq.losrecursionistas.backend.persistence.sql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import unq.losrecursionistas.backend.model.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioDAOSQL extends JpaRepository<Usuario, Long> {

	Optional<Usuario> findByNombreUsuario(String nombreUsuario);

	Optional<Usuario> findById(Long id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Usuario> findLockedById(Long id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Usuario> findFirstByAutoridadesContainingOrderByIdAsc(String autoridad);

	/** Menor id con la autoridad indicada, sin bloqueo (apto para consultas de solo lectura). */
	@Query("SELECT MIN(u.id) FROM Usuario u JOIN u.autoridades a WHERE a = :autoridad")
	Long buscarMenorIdConAutoridad(@Param("autoridad") String autoridad);

    @Query(value = "SELECT COUNT(*) > 0 FROM usuarios WHERE nombre_usuario = :nombreUsuario", nativeQuery = true)
    boolean existsByNombreUsuario(String nombreUsuario);
}