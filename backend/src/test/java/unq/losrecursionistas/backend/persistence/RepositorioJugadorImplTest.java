package unq.losrecursionistas.backend.persistence;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.exceptions.ExcepcionRecursoNoEncontrado;
import unq.losrecursionistas.backend.persistence.repository.impl.RepositorioJugadorImpl;
import unq.losrecursionistas.backend.persistence.sql.JugadorDAOSQL;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RepositorioJugadorImplTest {

    @Test
    void obtenerJugadorInexistenteLanzaRecursoNoEncontrado() {
        JugadorDAOSQL dao = mock(JugadorDAOSQL.class);
        when(dao.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new RepositorioJugadorImpl(dao).obtenerJugadorPorId(999L))
                .isInstanceOf(ExcepcionRecursoNoEncontrado.class)
                .hasMessage("No existe el jugador con el id: 999");
    }
}
