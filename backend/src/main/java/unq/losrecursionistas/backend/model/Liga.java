package unq.losrecursionistas.backend.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "ligas")
public class Liga {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private String nombre;
	@Column(nullable = false, unique = true)
	private String codigo;
	@Column(nullable = false)
	private boolean activa;

	public Liga(String nombre, String codigo, boolean activa) {
		this.nombre = nombre;
		this.codigo = codigo == null ? null : codigo.trim().toUpperCase();
		this.activa = activa;
	}

	public void validarDatosBasicos() {
		if (nombre == null || nombre.isBlank() || codigo == null || codigo.isBlank()) {
			throw new unq.losrecursionistas.backend.exceptions.DomainException(
					"LIGA_INVALIDA", "La liga requiere nombre y codigo");
		}
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (!(object instanceof Liga other)) return false;
		return Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() { return Objects.hash(id); }
}