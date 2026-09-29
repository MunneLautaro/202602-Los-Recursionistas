package unq.losrecursionistas.backend.model;

import jakarta.persistence.*;
import lombok.*;
import unq.losrecursionistas.backend.exceptions.ExcepcionDominio;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

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
		this.fechaCreacion = LocalDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires"));
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
		if (saldo < monto) {
			throw new ExcepcionDominio("Saldo insuficiente");
		}
		saldo = saldo - monto;
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
