package unq.losrecursionistas.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
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

import unq.losrecursionistas.backend.controller.dto.CredencialesLoginDto;
import unq.losrecursionistas.backend.controller.dto.RespuestaTokenDto;
import unq.losrecursionistas.backend.service.impl.AuthServiceImpl;
import unq.losrecursionistas.backend.service.interfaces.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

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
	void testLoginConCredencialesInvalidasLanzaExcepcion() {
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("Credenciales invalidas"));

		assertThatThrownBy(() -> authService.login(credenciales))
				.isInstanceOf(BadCredentialsException.class)
				.hasMessage("Credenciales invalidas");

		verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
	}
}

