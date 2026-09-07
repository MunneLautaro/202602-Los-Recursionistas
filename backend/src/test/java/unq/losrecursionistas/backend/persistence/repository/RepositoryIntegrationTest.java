package unq.losrecursionistas.backend.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Set;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.context.annotation.Import;

import unq.losrecursionistas.backend.exceptions.JugadorNoEncontradoException;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.model.Liga;
import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.JugadorRepository;
import unq.losrecursionistas.backend.persistence.repository.interfaces.UsuarioRepository;
import unq.losrecursionistas.backend.persistence.repository.impl.JugadorRepositoryImpl;
import unq.losrecursionistas.backend.persistence.repository.impl.UsuarioRepositoryImpl;

@DataJpaTest
@Import({UsuarioRepositoryImpl.class, JugadorRepositoryImpl.class})
class RepositoryIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JugadorRepository jugadorRepository;

    @Test
    void persisteYRecuperaUsuarioPorUsername() {
        Usuario usuario = new Usuario("ana", "hash", new BigDecimal("1000.00"), true, Set.of("USER"));

        Usuario guardado = usuarioRepository.guardar(usuario);

        assertEquals(usuario.getId(), guardado.getId());
        assertEquals(usuario.getId(), usuarioRepository.recuperarPorUsername("ana").getId());
    }

    @Test
    void recuperaJugadoresFiltradosYPaginados() {
        Liga liga = new Liga("Premier League", "PREMIER", true);
        entityManager.persist(liga);
        entityManager.persist(new Jugador("Jugador 1", "Equipo A", "DELANTERO", liga, true, true));
        entityManager.persist(new Jugador("Jugador 2", "Equipo A", "DEFENSOR", liga, true, true));
        entityManager.flush();

        Page<Jugador> resultado = jugadorRepository.recuperarJugadores(
                "Premier League", "Equipo A", null, true, PageRequest.of(0, 1));

        assertEquals(1, resultado.getContent().size());
        assertEquals(2, resultado.getTotalElements());
    }

    @Test
    void recuperarPorIdInexistenteLanzaExcepcionDescriptiva() {
        assertThrows(JugadorNoEncontradoException.class, () -> jugadorRepository.recuperarPorId(999L));
    }
}
