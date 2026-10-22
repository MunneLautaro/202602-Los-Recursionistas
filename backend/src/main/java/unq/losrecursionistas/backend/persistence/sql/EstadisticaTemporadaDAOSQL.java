package unq.losrecursionistas.backend.persistence.sql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import unq.losrecursionistas.backend.model.EstadisticaTemporada;

import java.time.LocalDate;
import java.util.List;

public interface EstadisticaTemporadaDAOSQL extends JpaRepository<EstadisticaTemporada, Long> {
    @Query("""
        select e from EstadisticaTemporada e
        join fetch e.jugador j
        join fetch j.equipo
        where j.id = :jugadorId
          and e.temporadaInicio = :temporadaInicio
        """)
    List<EstadisticaTemporada> findByJugadorIdAndTemporadaInicio(
            @Param("jugadorId") Long jugadorId,
            @Param("temporadaInicio") LocalDate temporadaInicio);

    @Query("""
        select e from EstadisticaTemporada e
        join fetch e.jugador j
        join fetch j.equipo equipo
        join equipo.liga liga
        where e.temporadaInicio = :temporadaInicio
          and (:liga is null or liga.nombre = :liga)
          and (:posicion is null or j.posicion = :posicion)
        order by e.ratingPromedio desc
        """)
    List<EstadisticaTemporada> buscarRanking(@Param("liga") String liga,
                                              @Param("posicion") String posicion,
                                              @Param("temporadaInicio") LocalDate temporadaInicio);
}
