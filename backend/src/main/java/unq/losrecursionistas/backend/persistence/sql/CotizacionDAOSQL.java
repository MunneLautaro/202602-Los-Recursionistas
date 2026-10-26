package unq.losrecursionistas.backend.persistence.sql;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import unq.losrecursionistas.backend.model.Cotizacion;

@Repository
public interface CotizacionDAOSQL extends JpaRepository<Cotizacion, Long> {
    java.util.Optional<Cotizacion> findFirstByJugadorIdOrderByTimestampCalculoDesc(Long jugadorId);
    @Query("""
            SELECT c FROM Cotizacion c
            WHERE c.jugador.id = :jugadorId
              AND c.timestampCalculo >= :desde
              AND c.timestampCalculo < :hasta
            """)
    Page<Cotizacion> buscarPorJugador(@Param("jugadorId") Long jugadorId,
                                      @Param("desde") java.time.LocalDateTime desde,
                                      @Param("hasta") java.time.LocalDateTime hasta,
                                      Pageable pageable);

    @Query(value = """
            WITH cotizaciones_ordenadas AS (
                SELECT c.*, ROW_NUMBER() OVER (
                    PARTITION BY c.jugador_id
                    ORDER BY c.timestamp_calculo DESC, c.id DESC
                ) AS fila
                FROM cotizaciones c
            )
            SELECT j.id AS jugadorId, j.nombre AS nombre, j.posicion AS posicion,
                   e.nombre AS equipo, l.nombre AS liga,
                   actual.valor AS valorActual, anterior.valor AS valorAnterior,
                   CASE WHEN anterior.valor IS NULL THEN NULL
                        ELSE ABS(actual.valor - anterior.valor) END AS variacion
            FROM jugadores j
            JOIN equipos e ON e.id = j.equipo_id
            LEFT JOIN ligas l ON l.id = e.liga_id
            JOIN cotizaciones_ordenadas actual
              ON actual.jugador_id = j.id AND actual.fila = 1
            LEFT JOIN cotizaciones_ordenadas anterior
              ON anterior.jugador_id = j.id AND anterior.fila = 2
            WHERE (:ligaId IS NULL OR l.id = :ligaId)
              AND (:posicion IS NULL OR
                   LOWER(REPLACE(REPLACE(REPLACE(
                       TRANSLATE(COALESCE(j.posicion, ''), 'ÁÉÍÓÚÜÑáéíóúüñ',
                                   'AEIOUUNaeiouun'), ' ', ''), '-', ''), '_', ''))
                   = :posicion)
            ORDER BY actual.valor DESC, j.id ASC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM jugadores j
            JOIN equipos e ON e.id = j.equipo_id
            LEFT JOIN ligas l ON l.id = e.liga_id
            WHERE (:ligaId IS NULL OR l.id = :ligaId)
              AND (:posicion IS NULL OR
                   LOWER(REPLACE(REPLACE(REPLACE(
                       TRANSLATE(COALESCE(j.posicion, ''), 'ÁÉÍÓÚÜÑáéíóúüñ',
                                   'AEIOUUNaeiouun'), ' ', ''), '-', ''), '_', ''))
                   = :posicion)
              AND EXISTS (SELECT 1 FROM cotizaciones c WHERE c.jugador_id = j.id)
            """,
            nativeQuery = true)
    Page<RankingProyeccion> buscarRanking(@Param("ligaId") Long ligaId,
                                          @Param("posicion") String posicion,
                                          Pageable pageable);

    interface RankingProyeccion {
        Long getJugadorId();
        String getNombre();
        String getPosicion();
        String getEquipo();
        String getLiga();
        java.math.BigDecimal getValorActual();
        java.math.BigDecimal getValorAnterior();
        java.math.BigDecimal getVariacion();
    }
}
