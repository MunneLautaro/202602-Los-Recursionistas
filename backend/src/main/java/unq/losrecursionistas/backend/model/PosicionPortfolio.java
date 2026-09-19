package unq.losrecursionistas.backend.model;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
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
@Table(name = "posiciones_portfolio")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class PosicionPortfolio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "jugador_id", nullable = false)
	private Jugador jugador;

	@Column(name = "cantidad_tokens", nullable = false, precision = 19, scale = 2)
	private BigDecimal cantidadTokens;

	@Column(name = "precio_compra", nullable = false, precision = 19, scale = 2)
	private BigDecimal precioCompra;

	@Column(name = "fecha_adquisicion", nullable = false)
	private Instant fechaAdquisicion;

	@Column(name = "fecha_actualizacion", nullable = false)
	private Instant fechaActualizacion;

	public PosicionPortfolio(Usuario usuario, Jugador jugador, BigDecimal cantidadTokens, BigDecimal precioCompra) {
		if (usuario == null || jugador == null) {
			throw new ExcepcionValidacion("El usuario y el jugador son obligatorios");
		}
		jugador.validarActivo();
		validarPositivo(cantidadTokens, "La cantidad de tokens debe ser positiva");
		validarPositivo(precioCompra, "El precio de compra debe ser positivo");
		this.usuario = usuario;
		this.jugador = jugador;
		this.cantidadTokens = cantidadTokens;
		this.precioCompra = precioCompra;
		this.fechaAdquisicion = Instant.now();
		this.fechaActualizacion = fechaAdquisicion;
	}

	public void comprar(BigDecimal tokens, BigDecimal precio) {
		jugador.validarActivo();
		validarPositivo(tokens, "La cantidad de tokens debe ser positiva");
		validarPositivo(precio, "El precio debe ser positivo");
		BigDecimal total = tokens.multiply(precio);
		usuario.debitar(total);
		cantidadTokens = cantidadTokens.add(tokens);
		precioCompra = precio;
		fechaActualizacion = Instant.now();
	}

	public void vender(BigDecimal tokens) {
		jugador.validarActivo();
		validarPositivo(tokens, "La cantidad de tokens debe ser positiva");
		if (cantidadTokens.compareTo(tokens) < 0) {
			throw new ExcepcionDominio("Tokens insuficientes");
		}
		cantidadTokens = cantidadTokens.subtract(tokens);
		usuario.acreditar(tokens.multiply(precioCompra));
		fechaActualizacion = Instant.now();
	}

	private static void validarPositivo(BigDecimal valor, String mensaje) {
		if (valor == null || valor.signum() <= 0) {
			throw new ExcepcionValidacion(mensaje);
		}
	}
}
