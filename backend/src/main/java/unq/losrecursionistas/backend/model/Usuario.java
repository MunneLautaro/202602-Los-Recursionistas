package unq.losrecursionistas.backend.model;

import jakarta.persistence.*;
import lombok.*;
import unq.losrecursionistas.backend.exceptions.ExcepcionDominio;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

	@Column(name = "saldo_reservado", nullable = false)
	@Builder.Default
	private Double saldoReservado = 0.0;

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
		this.saldoReservado = 0.0;
		this.fechaCreacion = LocalDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires"));
		this.habilitado = true;
		this.autoridades.add("ROLE_USUARIO");
	}

	public void debitar(Double monto) {
		validarMonto(monto);
		BigDecimal nuevoSaldo = dinero(saldo).subtract(dinero(monto));
		if (nuevoSaldo.signum() < 0) {
			throw new ExcepcionDominio("Saldo insuficiente");
		}
		saldo = normalizar(nuevoSaldo);
	}

	public void acreditar(Double monto) {
		validarMonto(monto);
		saldo = normalizar(dinero(saldo).add(dinero(monto)));
	}

	public void reservar(Double monto) {
		validarMonto(monto);
		BigDecimal reservado = dinero(saldoReservado).add(dinero(monto));
		if (reservado.compareTo(dinero(saldo)) > 0) {
			throw new ExcepcionDominio("Saldo disponible insuficiente");
		}
		saldoReservado = normalizar(reservado);
	}

	public void liberar(Double monto) {
		validarMonto(monto);
		BigDecimal nuevoReservado = dinero(saldoReservado).subtract(dinero(monto));
		if (nuevoReservado.signum() < 0) {
			throw new ExcepcionDominio("El saldo reservado no puede ser negativo");
		}
		saldoReservado = normalizar(nuevoReservado);
	}

	public Double saldoDisponible() {
		return normalizar(dinero(saldo).subtract(dinero(saldoReservado)));
	}

	private static void validarMonto(Double monto) {
		if (monto == null || monto <= 0) {
			throw new ExcepcionValidacion("El monto debe ser positivo");
		}
	}

	private static BigDecimal dinero(Double valor) {
		return BigDecimal.valueOf(valor == null ? 0.0 : valor).setScale(2, RoundingMode.HALF_UP);
	}

	private static Double normalizar(BigDecimal valor) {
		return valor.setScale(2, RoundingMode.HALF_UP).doubleValue();
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
