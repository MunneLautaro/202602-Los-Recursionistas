package unq.losrecursionistas.backend.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.impl.JugadorRepositoryImpl;
import unq.losrecursionistas.backend.persistence.repository.impl.UsuarioRepositoryImpl;
import unq.losrecursionistas.backend.persistence.repository.interfaces.UsuarioRepository;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@Import({UsuarioRepositoryImpl.class, JugadorRepositoryImpl.class})
class RepositoryPostgresIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void persisteUsuarioEnPostgresReal() {
        Usuario usuario = new Usuario("postgre", "hash", new BigDecimal("1000.00"), true, Set.of("USER"));

        Usuario guardado = usuarioRepository.guardar(usuario);

        assertEquals(usuario.getId(), guardado.getId());
        assertEquals(usuario.getId(), usuarioRepository.recuperarPorUsername("postgre").getId());
    }
}
