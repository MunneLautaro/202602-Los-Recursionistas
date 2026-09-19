package unq.losrecursionistas.backend.model;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import unq.losrecursionistas.backend.exceptions.ExcepcionDominio;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nombre_usuario", nullable = false, unique = true)
	private String nombreUsuario;

	@Column(nullable = false)
	private String contrasena;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal saldo;

	@Column(name = "fecha_creacion", nullable = false)
	private Instant fechaCreacion;

	public Usuario(String nombreUsuario, String contrasena, BigDecimal saldo) {
		validarTexto(nombreUsuario, "El nombre de usuario es obligatorio");
		validarTexto(contrasena, "La contrasena es obligatoria");
		if (saldo == null || saldo.signum() < 0) {
			throw new ExcepcionValidacion("El saldo no puede ser negativo");
		}
		this.nombreUsuario = nombreUsuario;
		this.contrasena = contrasena;
		this.saldo = saldo;
		this.fechaCreacion = Instant.now();
	}

	public void debitar(BigDecimal monto) {
		if (monto == null || monto.signum() <= 0) {
			throw new ExcepcionValidacion("El monto debe ser positivo");
		}
		if (saldo.compareTo(monto) < 0) {
			throw new ExcepcionDominio("Saldo insuficiente");
		}
		saldo = saldo.subtract(monto);
	}

	public void acreditar(BigDecimal monto) {
		if (monto == null || monto.signum() <= 0) {
			throw new ExcepcionValidacion("El monto debe ser positivo");
		}
		saldo = saldo.add(monto);
	}

	private static void validarTexto(String valor, String mensaje) {
		if (valor == null || valor.isBlank()) {
			throw new ExcepcionValidacion(mensaje);
		}
	}
}
