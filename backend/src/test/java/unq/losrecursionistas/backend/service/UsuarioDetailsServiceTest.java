package unq.losrecursionistas.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioUsuario;
import unq.losrecursionistas.backend.service.impl.UsuarioDetailsServiceImpl;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTest {

	@Mock
	private RepositorioUsuario repositorioUsuario;

	@InjectMocks
	private UsuarioDetailsServiceImpl usuarioDetailsService;

	private Usuario usuarioValido;

	@BeforeEach
	void setUp() {
		usuarioValido = new Usuario("jugador1", "hashPassword", BigDecimal.ZERO);
	}

	@Test
	void testCargarUsuarioPorNombreDeUsuarioExitoso() {
		when(repositorioUsuario.buscarPorNombreUsuario("jugador1")).thenReturn(usuarioValido);

		UserDetails detalles = usuarioDetailsService.loadUserByUsername("jugador1");

		assertThat(detalles.getUsername()).isEqualTo("jugador1");
		assertThat(detalles.getPassword()).isEqualTo("hashPassword");
		assertThat(detalles.getAuthorities()).extracting("authority").containsExactly("ROLE_USUARIO");
		assertThat(detalles.isEnabled()).isTrue();
	}

	@Test
	void testCargarUsuarioInexistenteLanzaExcepcion() {
		when(repositorioUsuario.buscarPorNombreUsuario("fantasma")).thenReturn(null);

		assertThatThrownBy(() -> usuarioDetailsService.loadUserByUsername("fantasma"))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessage("Credenciales invalidas");
	}

	@Test
	void testCargarUsuarioDeshabilitadoRetornaNoHabilitado() {
		usuarioValido.deshabilitar();
		when(repositorioUsuario.buscarPorNombreUsuario("jugador1")).thenReturn(usuarioValido);

		UserDetails detalles = usuarioDetailsService.loadUserByUsername("jugador1");

		assertThat(detalles.isEnabled()).isFalse();
	}
}