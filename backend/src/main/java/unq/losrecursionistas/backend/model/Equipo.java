package unq.losrecursionistas.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "equipos")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Equipo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nombre;
	private String codigo;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "liga_id", nullable = false)
	private Liga liga;

	public Equipo(String nombre, String codigo, Liga liga) {
		if (nombre == null || nombre.isBlank()) {
			throw new ExcepcionValidacion("El nombre del equipo es obligatorio");
		}
		if (codigo == null || codigo.isBlank()) {
			throw new ExcepcionValidacion("El codigo del equipo es obligatorio");
		}
		if (liga == null) {
			throw new ExcepcionValidacion("La liga del equipo es obligatoria");
		}
		this.nombre = nombre;
		this.codigo = codigo;
		this.liga = liga;
	}
}
