package unq.losrecursionistas.backend.persistence;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.model.JugadorFiltro;
import unq.losrecursionistas.backend.persistence.sql.JugadorSpecs;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JugadorSpecsTest {

    @Test
    void construyePredicadosParaTodosLosFiltros() {
        Root<Jugador> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        Path<String> nombre = mock(Path.class);
        Path<Object> posicion = mock(Path.class);
        Path<Object> equipo = mock(Path.class);
        Path<Object> liga = mock(Path.class);
        Path<Object> equipoId = mock(Path.class);
        Path<Object> ligaId = mock(Path.class);
        Predicate predicate = mock(Predicate.class);
        doReturn(nombre).when(root).get("nombre");
        when(root.get("posicion")).thenReturn(posicion);
        when(root.get("equipo")).thenReturn(equipo);
        when(equipo.get("id")).thenReturn(equipoId);
        when(equipo.get("liga")).thenReturn(liga);
        when(liga.get("id")).thenReturn(ligaId);
        Expression<String> lowerNombre = mock(Expression.class);
        when(builder.lower(nombre)).thenReturn(lowerNombre);
        when(builder.like(any(), anyString())).thenReturn(predicate);
        when(builder.equal(any(), any())).thenReturn(predicate);
        when(builder.and(any(Predicate[].class))).thenReturn(predicate);

        JugadorSpecs.conFiltro(JugadorFiltro.builder().nombre(" Ana ").posicion("DEL")
                .equipoId(1L).ligaId(2L).build()).toPredicate(root, query, builder);
        JugadorSpecs.conFiltro(null).toPredicate(root, query, builder);

        verify(builder).and(any(Predicate[].class));
        verify(builder).conjunction();
    }
}
