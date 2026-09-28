package unq.losrecursionistas.backend.persistence.sql;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.model.JugadorFiltro;

import java.util.ArrayList;
import java.util.List;

public class JugadorSpecs {

    public static Specification<Jugador> conFiltro(JugadorFiltro filtro) {
        return (root, query, cb) -> {
            if (filtro == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filtro.getNombre() != null && !filtro.getNombre().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("nombre")), "%" + filtro.getNombre().trim().toLowerCase() + "%"));
            }

            if (filtro.getPosicion() != null && !filtro.getPosicion().isBlank()) {
                predicates.add(cb.equal(root.get("posicion"), filtro.getPosicion()));
            }

            if (filtro.getEquipoId() != null) {
                predicates.add(cb.equal(root.get("equipo").get("id"), filtro.getEquipoId()));
            }

            if (filtro.getLigaId() != null) {
                predicates.add(cb.equal(root.get("equipo").get("liga").get("id"), filtro.getLigaId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
