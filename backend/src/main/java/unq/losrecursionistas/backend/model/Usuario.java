package unq.losrecursionistas.backend.model;

import java.math.BigDecimal;
import java.util.Set;

public final class Usuario {

	private final Long id;
	private final String username;
	private final String apiKeyHash;
	private final BigDecimal saldo;
	private final boolean habilitado;
	private final Set<String> roles;

	public Usuario(Long id, String username, String apiKeyHash, BigDecimal saldo, boolean habilitado, Set<String> roles) {
		if (username == null || username.isBlank() || apiKeyHash == null || apiKeyHash.isBlank()) {
			throw new IllegalArgumentException("El usuario requiere username y ApiKey");
		}
		if (saldo == null || saldo.signum() < 0) {
			throw new IllegalArgumentException("El saldo no puede ser negativo");
		}
		this.id = id;
		this.username = username.trim();
		this.apiKeyHash = apiKeyHash;
		this.saldo = saldo;
		this.habilitado = habilitado;
		this.roles = Set.copyOf(roles);
	}

	public Long id() { return id; }
	public String username() { return username; }
	public String apiKeyHash() { return apiKeyHash; }
	public BigDecimal saldo() { return saldo; }
	public boolean habilitado() { return habilitado; }
	public Set<String> roles() { return roles; }
}