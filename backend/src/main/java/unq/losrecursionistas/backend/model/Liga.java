package unq.losrecursionistas.backend.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ligas")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Liga {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String nombre;

	@Column(nullable = false, unique = true)
	private String codigo;

	@Column(name = "fecha_creacion", nullable = false)
	private Instant fechaCreacion;

	public Liga(String nombre, String codigo) {
		validarTexto(nombre, "El nombre de la liga es obligatorio");
		validarTexto(codigo, "El codigo de la liga es obligatorio");
		this.nombre = nombre;
		this.codigo = codigo;
		this.fechaCreacion = Instant.now();
	}

	private static void validarTexto(String valor, String mensaje) {
		if (valor == null || valor.isBlank()) {
			throw new ExcepcionValidacion(mensaje);
		}
	}
}
