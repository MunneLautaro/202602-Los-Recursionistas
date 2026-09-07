package unq.losrecursionistas.backend.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class ApiKeyService {

	public String generar() {
		return UUID.randomUUID().toString();
	}

	public String hash(String apiKey) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
					.digest(apiKey.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("No se pudo proteger la ApiKey", exception);
		}
	}

	public boolean coincide(String apiKey, String hashEsperado) {
		return hash(apiKey).equals(hashEsperado);
	}
}
