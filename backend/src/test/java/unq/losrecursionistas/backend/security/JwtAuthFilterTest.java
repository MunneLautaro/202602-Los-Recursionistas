package unq.losrecursionistas.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import jakarta.servlet.FilterChain;
import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.security.jwt.impl.JwtAuthFilter;
import unq.losrecursionistas.backend.security.jwt.impl.JwtServiceImpl;

class JwtAuthFilterTest {

	private static final String SECRET = "clave-local-de-desarrollo-de-32-bytes-minimo";

	private final JwtServiceImpl jwtService = new JwtServiceImpl(SECRET);
	private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
	private final FilterChain chain = mock(FilterChain.class);
	private final JwtAuthFilter filter = new JwtAuthFilter(jwtService, userDetailsService);
	private final UserDetails usuario = org.springframework.security.core.userdetails.User
			.withUsername("jugador-filtro").password("hash").roles("USUARIO").build();

	@BeforeEach
	void preparar() {
		when(userDetailsService.loadUserByUsername("jugador-filtro")).thenReturn(usuario);
	}

	@AfterEach
	void limpiarContexto() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void cargaIdentidadConBearerValido() throws Exception {
		var request = requestConAuthorization("Bearer " + jwtService.generarToken(usuario));
		var response = new MockHttpServletResponse();

		filter.doFilter(request, response, chain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
		assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("jugador-filtro");
		verify(chain).doFilter(eq(request), eq(response));
	}

	@Test
	void ignoraHeaderAusenteEsquemaIncorrectoYBearerVacio() throws Exception {
		for (String header : new String[] { null, "Basic credencial", "Bearer " }) {
			SecurityContextHolder.clearContext();
			var request = requestConAuthorization(header);

			filter.doFilter(request, new MockHttpServletResponse(), chain);

			assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		}
		verify(chain, org.mockito.Mockito.times(3)).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
		verifyNoInteractions(userDetailsService);
	}

	@Test
	void rechazaTokenMalformadoFirmaInvalidaExpiradoSubjectAusenteUsuarioInexistenteEInhabilitado() throws Exception {
		var expirado = firmar(new JWTClaimsSet.Builder()
				.subject("jugador-filtro")
				.issueTime(Date.from(Instant.now().minusSeconds(7200)))
				.expirationTime(Date.from(Instant.now().minusSeconds(3600)))
				.build());
		var sinSubject = firmar(new JWTClaimsSet.Builder()
				.issueTime(new Date())
				.expirationTime(Date.from(Instant.now().plusSeconds(3600)))
				.build());
		var usuarioInexistente = org.springframework.security.core.userdetails.User.withUsername("fantasma")
				.password("hash").roles("USUARIO").build();
		var usuarioInhabilitado = org.springframework.security.core.userdetails.User.withUsername("inhabilitado")
				.password("hash").roles("USUARIO").disabled(true).build();
		when(userDetailsService.loadUserByUsername("fantasma"))
				.thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("no revelar"));
		when(userDetailsService.loadUserByUsername("inhabilitado")).thenReturn(usuarioInhabilitado);

		var tokens = new String[] {
				"malformado",
				jwtService.generarToken(usuario) + "x",
				expirado,
				sinSubject,
				jwtService.generarToken(usuarioInexistente),
				jwtService.generarToken(usuarioInhabilitado)
		};
		for (String token : tokens) {
			SecurityContextHolder.clearContext();
			filter.doFilter(requestConAuthorization("Bearer " + token), new MockHttpServletResponse(), chain);
			assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		}

		when(userDetailsService.loadUserByUsername("jugador-filtro")).thenThrow(new RuntimeException("no revelar"));
		SecurityContextHolder.clearContext();
		filter.doFilter(requestConAuthorization("Bearer " + jwtService.generarToken(usuario)),
				new MockHttpServletResponse(), chain);
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	private static String firmar(JWTClaimsSet claims) throws Exception {
		var token = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
		token.sign(new MACSigner(SECRET));
		return token.serialize();
	}

	private static MockHttpServletRequest requestConAuthorization(String header) {
		var request = new MockHttpServletRequest();
		if (header != null) {
			request.addHeader("Authorization", header);
		}
		return request;
	}
}