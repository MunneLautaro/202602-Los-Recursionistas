package unq.losrecursionistas.backend.persistence.sql;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import unq.losrecursionistas.backend.model.TenenciaToken;

import java.util.Optional;

@Repository
public interface TenenciaTokenDAOSQL extends JpaRepository<TenenciaToken, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TenenciaToken> findByUsuarioIdAndJugadorId(Long usuarioId, Long jugadorId);

    Page<TenenciaToken> findByUsuarioId(Long usuarioId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.cantidadTotal), 0) FROM TenenciaToken t WHERE t.usuario.id <> :usuarioId")
    long sumarExcluyendoUsuario(@Param("usuarioId") Long usuarioId);
}
