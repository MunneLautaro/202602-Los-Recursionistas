package unq.losrecursionistas.backend.persistence.sql;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import unq.losrecursionistas.backend.model.EstadoOrden;
import unq.losrecursionistas.backend.model.Orden;
import unq.losrecursionistas.backend.model.TipoOrden;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenDAOSQL extends JpaRepository<Orden, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select o from Orden o
            where o.jugador.id = :jugadorId and o.tipo = :tipo
              and o.precioUnitario = :precio and o.estado = :estado
              and o.usuario.id <> :usuarioId
            order by o.fechaCreacion asc, o.id asc
            """)
    List<Orden> buscarCompatiblesBloqueadas(@Param("jugadorId") Long jugadorId,
                                             @Param("tipo") TipoOrden tipo,
                                             @Param("precio") BigDecimal precio,
                                             @Param("estado") EstadoOrden estado,
                                             @Param("usuarioId") Long usuarioId);

    Page<Orden> findByEstado(EstadoOrden estado, Pageable pageable);

    Page<Orden> findByJugadorIdAndEstado(Long jugadorId, EstadoOrden estado, Pageable pageable);

    Page<Orden> findByTipoAndEstado(TipoOrden tipo, EstadoOrden estado, Pageable pageable);

    Page<Orden> findByJugadorIdAndTipoAndEstado(Long jugadorId, TipoOrden tipo,
                                                EstadoOrden estado, Pageable pageable);

    Page<Orden> findByUsuarioId(Long usuarioId, Pageable pageable);

    @Query("""
            select o from Orden o
            where o.estado = :estado
              and (:jugadorId is null or o.jugador.id = :jugadorId)
              and (:tipo is null or o.tipo = :tipo)
              and lower(o.jugador.nombre) like lower(concat('%', coalesce(:jugadorNombre, ''), '%'))
            """)
    Page<Orden> buscarActivasConNombre(@Param("jugadorId") Long jugadorId,
                                       @Param("tipo") TipoOrden tipo,
                                       @Param("jugadorNombre") String jugadorNombre,
                                       @Param("estado") EstadoOrden estado,
                                       Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Orden> findFirstByUsuarioIdAndJugadorIdAndEsLiquidezInicialTrue(
            Long usuarioId, Long jugadorId);
}
