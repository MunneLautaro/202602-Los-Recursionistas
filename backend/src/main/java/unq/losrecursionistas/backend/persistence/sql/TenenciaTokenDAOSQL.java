package unq.losrecursionistas.backend.persistence.sql;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import unq.losrecursionistas.backend.model.TenenciaToken;

import java.util.Optional;

@Repository
public interface TenenciaTokenDAOSQL extends JpaRepository<TenenciaToken, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TenenciaToken> findByUsuarioIdAndJugadorId(Long usuarioId, Long jugadorId);

    Page<TenenciaToken> findByUsuarioId(Long usuarioId, Pageable pageable);
}
