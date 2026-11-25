package unq.losrecursionistas.backend.persistence.sql;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import unq.losrecursionistas.backend.model.TransaccionMercado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransaccionMercadoDAOSQL extends JpaRepository<TransaccionMercado, Long> {
    Page<TransaccionMercado> findByCompradorIdOrVendedorIdOrderByEjecutadaEnDesc(
            Long compradorId, Long vendedorId, Pageable pageable);

    /** Emparejamientos y volumen operado agrupados por dia argentino. */
    @Query(value = """
            SELECT CAST(t.ejecutada_en AT TIME ZONE 'America/Argentina/Buenos_Aires' AS DATE) AS dia,
                   COUNT(*) AS cantidad, COALESCE(SUM(t.total), 0) AS volumen
            FROM transacciones_mercado t
            WHERE CAST(t.ejecutada_en AT TIME ZONE 'America/Argentina/Buenos_Aires' AS DATE)
                  BETWEEN :desde AND :hasta
            GROUP BY 1
            """, nativeQuery = true)
    List<TransaccionesPorDiaProyeccion> resumirPorDia(@Param("desde") LocalDate desde,
                                                      @Param("hasta") LocalDate hasta);

    interface TransaccionesPorDiaProyeccion {
        Object getDia();
        Number getCantidad();
        BigDecimal getVolumen();
    }
}
