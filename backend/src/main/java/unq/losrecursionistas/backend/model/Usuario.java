package unq.losrecursionistas.backend.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import unq.losrecursionistas.backend.exceptions.DomainException;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "usuarios")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, unique = true, length = 80)
	private String username;
	@Column(nullable = false)
	private String apiKeyHash;
	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal saldo;
	@Column(nullable = false)
	private boolean habilitado;
	@Builder.Default
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"))
	@Column(name = "rol", nullable = false)
	private Set<String> roles = new HashSet<>();

	public Usuario(String username, String apiKeyHash, BigDecimal saldo, boolean habilitado, Set<String> roles) {
		this.username = username;
		this.apiKeyHash = apiKeyHash;
		this.saldo = saldo;
		this.habilitado = habilitado;
		this.roles = roles == null ? new HashSet<>() : new HashSet<>(roles);
	}

	public void validarDatosBasicos() {
		if (username == null || username.isBlank() || apiKeyHash == null || apiKeyHash.isBlank()) {
			throw new DomainException("USUARIO_INVALIDO", "El usuario requiere username y ApiKey");
		}
		if (saldo == null || saldo.signum() < 0) {
			throw new DomainException("SALDO_INVALIDO", "El saldo no puede ser negativo");
		}
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (!(object instanceof Usuario other)) return false;
		return Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}