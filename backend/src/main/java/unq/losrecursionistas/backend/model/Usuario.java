package unq.losrecursionistas.backend.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.*;
import unq.losrecursionistas.backend.exceptions.ExcepcionDominio;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nombre_usuario", nullable = false, unique = true)
	private String nombreUsuario;

	@Column(nullable = false)
	private String contrasena;

	@Column(nullable = false)
	private Double saldo;

	@Column(name = "fecha_creacion", nullable = false)
	private LocalDateTime fechaCreacion;

	@Column(nullable = false)
	private boolean habilitado;

	@ElementCollection
	@CollectionTable(name = "usuarios_autoridades", joinColumns = @JoinColumn(name = "usuario_id"))
	@Column(name = "autoridad", nullable = false)
	private Set<String> autoridades = new HashSet<>();

	public Usuario(String nombreUsuario, String contrasena, Double saldo) {
		validarTexto(nombreUsuario, "El nombre de usuario es obligatorio");
		validarTexto(contrasena, "La contrasena es obligatoria");
		if (saldo == null || saldo < 0) {
			throw new ExcepcionValidacion("El saldo no puede ser negativo");
		}
		this.nombreUsuario = nombreUsuario;
		this.contrasena = contrasena;
		this.saldo = saldo;
		this.fechaCreacion = LocalDateTime.now();
		this.habilitado = true;
		this.autoridades.add("ROLE_USUARIO");
	}

	public void debitar(Double monto) {
		if (monto == null || monto <= 0) {
			throw new ExcepcionValidacion("El monto debe ser positivo");
		}
		if (saldo < 0) {
			throw new ExcepcionDominio("Saldo insuficiente");
		}
		saldo =- monto;
	}

	public void acreditar(Double monto) {
		if (monto == null || monto <= 0) {
			throw new ExcepcionValidacion("El monto debe ser positivo");
		}
		saldo = saldo + monto;
	}

	public void deshabilitar() {
		habilitado = false;
	}

	public void habilitar() {
		habilitado = true;
	}

	private static void validarTexto(String valor, String mensaje) {
		if (valor == null || valor.isBlank()) {
			throw new ExcepcionValidacion(mensaje);
		}
	}
}
