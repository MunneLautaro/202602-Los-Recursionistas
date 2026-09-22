package unq.losrecursionistas.backend.security.jwt.impl;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import unq.losrecursionistas.backend.service.interfaces.JwtService;

@Service
public class JwtServiceImpl implements JwtService {

	private static final long DURACION_SEGUNDOS = 3600;
	private static final JWSAlgorithm ALGORITMO = JWSAlgorithm.HS256;

	private final byte[] secreto;

	public JwtServiceImpl(@Value("${jwt.secret}") String secreto) {
		this.secreto = secreto.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public String generarToken(UserDetails usuario) {
		Instant emision = Instant.now();
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.subject(usuario.getUsername())
				.issueTime(Date.from(emision))
				.expirationTime(Date.from(emision.plusSeconds(DURACION_SEGUNDOS)))
				.build();
		SignedJWT token = new SignedJWT(new JWSHeader.Builder(ALGORITMO).build(), claims);
		try {
			token.sign(new MACSigner(secreto));
			return token.serialize();
		} catch (JOSEException excepcion) {
			throw new IllegalStateException("No se pudo generar el token", excepcion);
		}
	}

	@Override
	public Optional<String> extraerUsername(String token) {
		return leerClaims(token).map(JWTClaimsSet::getSubject).filter(subject -> !subject.isBlank());
	}

	@Override
	public boolean tokenValido(String token, UserDetails usuario) {
		return leerClaimsVerificados(token)
				.filter(claims -> usuario.getUsername().equals(claims.getSubject()))
				.filter(claims -> claims.getExpirationTime() != null)
				.filter(claims -> claims.getExpirationTime().toInstant().isAfter(Instant.now()))
				.isPresent();
	}

	private Optional<JWTClaimsSet> leerClaims(String token) {
		try {
			return Optional.of(SignedJWT.parse(token).getJWTClaimsSet());
		} catch (ParseException excepcion) {
			return Optional.empty();
		}
	}

	private Optional<JWTClaimsSet> leerClaimsVerificados(String token) {
		try {
			SignedJWT jwt = SignedJWT.parse(token);
			if (!ALGORITMO.equals(jwt.getHeader().getAlgorithm())) {
				return Optional.empty();
			}
			JWSVerifier verificador = new MACVerifier(secreto);
			if (!jwt.verify(verificador)) {
				return Optional.empty();
			}
			return Optional.of(jwt.getJWTClaimsSet());
		} catch (ParseException | JOSEException excepcion) {
			return Optional.empty();
		}
	}
}