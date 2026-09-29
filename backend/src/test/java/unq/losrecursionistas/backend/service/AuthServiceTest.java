package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import unq.losrecursionistas.backend.controller.dto.CredencialesLoginDto;
import unq.losrecursionistas.backend.controller.dto.RespuestaTokenDto;
import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.service.impl.AuthServiceImpl;
import unq.losrecursionistas.backend.service.impl.exceptions.ExcepcionNombreDeUsuarioExistente;
import unq.losrecursionistas.backend.service.interfaces.JwtService;
import unq.losrecursionistas.backend.service.interfaces.UsuarioService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UsuarioService usuarioService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtService jwtService;

	@Mock
	private Authentication autenticacion;

	@InjectMocks
	private AuthServiceImpl authService;

	private CredencialesLoginDto credenciales;
	private UserDetails userDetails;

	@BeforeEach
	void setUp() {
		credenciales = new CredencialesLoginDto("usuarioTest", "clave123");
		userDetails = User.withUsername("usuarioTest")
				.password("clave123")
				.authorities("ROLE_USUARIO")
				.build();
	}

	@Test
	@DisplayName("Caso Feliz: registrarUsuario encripta la contraseña y delega al usuarioService")
	void registrarUsuarioExitoso() {
		Usuario usuarioSinEncriptar = new Usuario("nuevoUsuario", "plainPassword", 100.0);
		when(passwordEncoder.encode("plainPassword")).thenReturn("hashedPassword");
		when(usuarioService.crearUsuario(usuarioSinEncriptar)).thenReturn(usuarioSinEncriptar);

		Usuario resultado = authService.registrarUsuario(usuarioSinEncriptar);

		assertThat(resultado).isNotNull();
		assertThat(resultado.getContrasena()).isEqualTo("hashedPassword");
		verify(passwordEncoder).encode("plainPassword");
		verify(usuarioService).crearUsuario(usuarioSinEncriptar);
	}

	@Test
	@DisplayName("Caso No Feliz: registrarUsuario lanza excepción si el nombre de usuario ya existe")
	void registrarUsuarioExistenteLanzaExcepcion() {
		Usuario usuarioExistente = new Usuario("usuarioRepetido", "plainPassword", 100.0);
		when(passwordEncoder.encode("plainPassword")).thenReturn("hashedPassword");
		when(usuarioService.crearUsuario(usuarioExistente)).thenThrow(new ExcepcionNombreDeUsuarioExistente(usuarioExistente));

		assertThatThrownBy(() -> authService.registrarUsuario(usuarioExistente))
				.isInstanceOf(ExcepcionNombreDeUsuarioExistente.class)
				.hasMessageContaining("usuarioRepetido");

		verify(passwordEncoder).encode("plainPassword");
		verify(usuarioService).crearUsuario(usuarioExistente);
	}

	@Test
	@DisplayName("Caso Feliz: login con credenciales válidas retorna token JWT")
	void testLoginConCredencialesValidasRetornaToken() {
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(autenticacion);
		when(autenticacion.getPrincipal()).thenReturn(userDetails);
		when(jwtService.generarToken(userDetails)).thenReturn("jwt.token.valido");

		RespuestaTokenDto respuesta = authService.login(credenciales);

		assertThat(respuesta).isNotNull();
		assertThat(respuesta.token()).isEqualTo("jwt.token.valido");
		verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
		verify(jwtService).generarToken(userDetails);
	}

	@Test
	@DisplayName("Caso No Feliz: login con credenciales inválidas lanza BadCredentialsException")
	void testLoginConCredencialesInvalidasLanzaExcepcion() {
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("Credenciales invalidas"));

		assertThatThrownBy(() -> authService.login(credenciales))
				.isInstanceOf(BadCredentialsException.class)
				.hasMessage("Credenciales invalidas");

		verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
	}
}
