package unq.losrecursionistas.backend.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioUsuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserInitializerTest {

    @Mock
    private RepositorioUsuario repositorioUsuario;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserInitializer adminUserInitializer;

    @Test
    @DisplayName("Debe crear usuario admin si no existe")
    void debeCrearAdminSiNoExiste() {
        when(repositorioUsuario.existeElUsuario("admin")).thenReturn(false);
        when(passwordEncoder.encode("admin123")).thenReturn("encodedPassword123");

        adminUserInitializer.run();

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repositorioUsuario).crearUsuario(captor.capture());

        Usuario guardado = captor.getValue();
        assertThat(guardado.getNombreUsuario()).isEqualTo("admin");
        assertThat(guardado.getContrasena()).isEqualTo("encodedPassword123");
        assertThat(guardado.getAutoridades()).contains("ROLE_ADMIN", "ROLE_USUARIO");
    }

    @Test
    @DisplayName("No debe crear usuario admin si ya existe")
    void noDebeCrearAdminSiYaExiste() {
        when(repositorioUsuario.existeElUsuario("admin")).thenReturn(true);

        adminUserInitializer.run();

        verify(repositorioUsuario, never()).crearUsuario(any());
        verify(passwordEncoder, never()).encode(any());
    }
}

