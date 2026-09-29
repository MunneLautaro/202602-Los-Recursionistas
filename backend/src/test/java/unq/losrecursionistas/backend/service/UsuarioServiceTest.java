package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioUsuario;
import unq.losrecursionistas.backend.service.impl.UsuarioServiceImpl;
import unq.losrecursionistas.backend.service.impl.exceptions.ExcepcionNombreDeUsuarioExistente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private RepositorioUsuario repositorioUsuario;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuarioNuevo;

    @BeforeEach
    void setUp() {
        usuarioNuevo = new Usuario("usuarioPrueba", "contrasena123", 500.0);
    }

    @Test
    @DisplayName("Caso Feliz: crearUsuario guarda exitosamente cuando el nombre de usuario no existe")
    void crearUsuarioExitoso() {
        when(repositorioUsuario.existeElUsuario("usuarioPrueba")).thenReturn(false);
        when(repositorioUsuario.crearUsuario(usuarioNuevo)).thenReturn(usuarioNuevo);

        Usuario usuarioCreado = usuarioService.crearUsuario(usuarioNuevo);

        assertThat(usuarioCreado).isNotNull();
        assertThat(usuarioCreado.getNombreUsuario()).isEqualTo("usuarioPrueba");
        verify(repositorioUsuario).existeElUsuario("usuarioPrueba");
        verify(repositorioUsuario).crearUsuario(usuarioNuevo);
    }

    @Test
    @DisplayName("Caso No Feliz: crearUsuario lanza ExcepcionNombreDeUsuarioExistente cuando el usuario ya existe")
    void crearUsuarioNombreExistenteLanzaExcepcion() {
        when(repositorioUsuario.existeElUsuario("usuarioPrueba")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.crearUsuario(usuarioNuevo))
                .isInstanceOf(ExcepcionNombreDeUsuarioExistente.class)
                .hasMessage("El nombre de usuario ya existe: usuarioPrueba");

        verify(repositorioUsuario).existeElUsuario("usuarioPrueba");
        verify(repositorioUsuario, never()).crearUsuario(usuarioNuevo);
    }
}
