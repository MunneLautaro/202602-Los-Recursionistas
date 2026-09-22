package unq.losrecursionistas.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioUsuario;
import unq.losrecursionistas.backend.service.impl.UsuarioDetailsServiceImpl;

class UsuarioDetailsServiceTest {

	private final RepositorioUsuario repositorio = mock(RepositorioUsuario.class);
	private final UsuarioDetailsServiceImpl servicio = new UsuarioDetailsServiceImpl(repositorio);

	@Test
	void cargaUsuarioConContrasenaYAutoridad() {
		var usuario = new Usuario("jugador1", "hash", BigDecimal.ZERO);
		when(repositorio.buscarPorNombreUsuario("jugador1")).thenReturn(Optional.of(usuario));

		var detalles = servicio.loadUserByUsername("jugador1");

		assertThat(detalles.getUsername()).isEqualTo("jugador1");
		assertThat(detalles.getPassword()).isEqualTo("hash");
		assertThat(detalles.getAuthorities()).extracting("authority").containsExactly("ROLE_USUARIO");
		assertThat(detalles.isEnabled()).isTrue();
	}

	@Test
	void rechazaUsuarioInexistenteSinExponerDetalles() {
		when(repositorio.buscarPorNombreUsuario("fantasma")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> servicio.loadUserByUsername("fantasma"))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessage("Credenciales invalidas");
	}

	@Test
	void representaUsuarioInhabilitadoComoNoHabilitado() {
		var usuario = new Usuario("jugador1", "hash", BigDecimal.ZERO);
		usuario.deshabilitar();
		when(repositorio.buscarPorNombreUsuario("jugador1")).thenReturn(Optional.of(usuario));

		var detalles = servicio.loadUserByUsername("jugador1");

		assertThat(detalles.isEnabled()).isFalse();
	}
}