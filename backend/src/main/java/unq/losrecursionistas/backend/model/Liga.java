package unq.losrecursionistas.backend.model;

import jakarta.persistence.*;
import lombok.*;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;

import java.time.LocalDate;
import java.time.ZoneId;

@Entity
@Table(name = "ligas")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Liga {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "id_externo")
	private Long idExterno;

	@Column(nullable = false, unique = true)
	private String nombre;

	@Column(nullable = false, unique = true)
	private String codigo;

	@Column(name = "fecha_creacion", nullable = false)
	private LocalDate fechaCreacion;

	public Liga(String nombre, String codigo) {
		validarTexto(nombre, "El nombre de la liga es obligatorio");
		validarTexto(codigo, "El codigo de la liga es obligatorio");
		this.nombre = nombre;
		this.codigo = codigo;
		this.fechaCreacion = LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires"));
	}

	public Liga(Long idExterno, String nombre, String codigo) {
		validarTexto(nombre, "El nombre de la liga es obligatorio");
		validarTexto(codigo, "El codigo de la liga es obligatorio");
		this.idExterno = idExterno;
		this.nombre = nombre;
		this.codigo = codigo;
		this.fechaCreacion = LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires"));
	}

	private static void validarTexto(String valor, String mensaje) {
		if (valor == null || valor.isBlank()) {
			throw new ExcepcionValidacion(mensaje);
		}
	}
}
