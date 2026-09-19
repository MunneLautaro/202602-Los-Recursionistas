package unq.losrecursionistas.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import unq.losrecursionistas.backend.exceptions.ExcepcionDominio;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "jugadores")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Jugador {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nombre;
	private String posicion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "equipo_id", nullable = false)
	private Equipo equipo;

	private boolean activo;

	public Jugador(String nombre, String posicion, Equipo equipo, boolean activo) {
		if (nombre == null || nombre.isBlank() || posicion == null || posicion.isBlank()) {
			throw new ExcepcionValidacion("El nombre y la posicion son obligatorios");
		}
		if (equipo == null) {
			throw new ExcepcionValidacion("El equipo del jugador es obligatorio");
		}
		this.nombre = nombre;
		this.posicion = posicion;
		this.equipo = equipo;
		this.activo = activo;
	}

	void validarActivo() {
		if (!activo) {
			throw new ExcepcionDominio("El jugador no esta activo");
		}
	}
}
