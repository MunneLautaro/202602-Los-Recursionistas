package unq.losrecursionistas.backend.model;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import unq.losrecursionistas.backend.exceptions.DomainException;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "jugadores")
public class Jugador {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String nombre;
	@Builder.Default
	private String equipo = "";
	@Builder.Default
	private String posicion = "";
	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "liga_id", nullable = false)
	private Liga liga;
	private boolean activo;
	private boolean disponible;

	public Jugador(String nombre, String equipo, String posicion, Liga liga,
			boolean activo, boolean disponible) {
		this.nombre = nombre;
		this.equipo = normalize(equipo);
		this.posicion = normalize(posicion);
		this.liga = liga;
		this.activo = activo;
		this.disponible = disponible;
	}

	private String normalize(String value) {
		return value == null ? "" : value.trim();
	}

	public void validarDatosBasicos() {
		if (nombre == null || nombre.isBlank() || liga == null) {
			throw new DomainException("JUGADOR_INVALIDO", "El jugador requiere nombre y liga");
		}
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof Jugador other && Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() { return Objects.hash(id); }
}