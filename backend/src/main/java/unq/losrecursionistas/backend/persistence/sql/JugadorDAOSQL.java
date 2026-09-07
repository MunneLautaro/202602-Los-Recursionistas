package unq.losrecursionistas.backend.persistence.sql;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import unq.losrecursionistas.backend.model.Jugador;

public interface JugadorDAOSQL extends JpaRepository<Jugador, Long> {

        @EntityGraph(attributePaths = "liga")
    @Query("""
            select jugador from Jugador jugador
            where (:liga is null or lower(jugador.liga.nombre) = lower(:liga))
              and (:equipo is null or lower(jugador.equipo) = lower(:equipo))
              and (:posicion is null or lower(jugador.posicion) = lower(:posicion))
              and (:activo is null or jugador.activo = :activo)
            order by jugador.id
            """)
    Page<Jugador> buscarPorFiltros(@Param("liga") String liga,
            @Param("equipo") String equipo,
            @Param("posicion") String posicion,
            @Param("activo") Boolean activo,
            Pageable pageable);
}
