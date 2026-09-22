package unq.losrecursionistas.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.text.ParseException;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import unq.losrecursionistas.backend.security.jwt.impl.JwtServiceImpl;

class JwtServiceImplTest {

	private static final String SECRET = "clave-local-de-desarrollo-de-32-bytes-minimo";

	private JwtServiceImpl servicio;
	private UserDetails usuario;

	@BeforeEach
	void preparar() {
		servicio = new JwtServiceImpl(SECRET);
		usuario = User.withUsername("jugador1").password("hash").roles("USUARIO").build();
	}

	@Test
	void generaTokenHs256ConClaimsYVigenciaDeUnaHora() throws Exception {
		var antes = Instant.now();
		var token = servicio.generarToken(usuario);
		var despues = Instant.now();

		assertThat(token).isNotBlank();
		assertThat(parse(token).getHeader().getAlgorithm()).isEqualTo(JWSAlgorithm.HS256);
		assertThat(servicio.extraerUsername(token)).contains("jugador1");
		assertThat(servicio.tokenValido(token, usuario)).isTrue();
		var claims = parse(token).getJWTClaimsSet();
		assertThat(claims.getIssueTime().toInstant()).isBetween(antes.minusSeconds(1), despues);
		assertThat(claims.getExpirationTime().toInstant())
				.isEqualTo(claims.getIssueTime().toInstant().plusSeconds(3600));
	}

	@Test
	void rechazaTokenMalformadoFirmaInvalidaExpiradoYSinSubject() throws Exception {
		assertThat(servicio.extraerUsername("malformado")).isEmpty();
		assertThat(servicio.tokenValido("malformado", usuario)).isFalse();
		assertThat(servicio.tokenValido(servicio.generarToken(usuario) + "x", usuario)).isFalse();
		var expirado = firmar(new JWTClaimsSet.Builder()
				.subject("jugador1")
				.issueTime(Date.from(Instant.now().minusSeconds(7200)))
				.expirationTime(Date.from(Instant.now().minusSeconds(3600)))
				.build());
		var sinSubject = firmar(new JWTClaimsSet.Builder()
				.issueTime(new Date())
				.expirationTime(Date.from(Instant.now().plusSeconds(3600)))
				.build());
		assertThat(servicio.tokenValido(expirado, usuario)).isFalse();
		assertThat(servicio.extraerUsername(sinSubject)).isEmpty();
	}

	private static String firmar(JWTClaimsSet claims) throws Exception {
		var token = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
		token.sign(new MACSigner(SECRET));
		return token.serialize();
	}

	private static SignedJWT parse(String token) {
		try {
			return SignedJWT.parse(token);
		} catch (ParseException excepcion) {
			throw new AssertionError(excepcion);
		}
	}
}