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
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cotizaciones_jugador")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class CotizacionJugador {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "jugador_id", nullable = false)
	private Jugador jugador;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal valor;

	@Column(name = "tokens_disponibles", nullable = false, precision = 19, scale = 2)
	private BigDecimal tokensDisponibles;

	@Column(name = "fecha_cotizacion", nullable = false)
	private Instant fechaCotizacion;

	public CotizacionJugador(Jugador jugador, BigDecimal valor, BigDecimal tokensDisponibles) {
		if (jugador == null) {
			throw new ExcepcionValidacion("El jugador es obligatorio");
		}
		this.jugador = jugador;
		this.fechaCotizacion = Instant.now();
		actualizar(valor, tokensDisponibles);
	}

	public void actualizar(BigDecimal valor, BigDecimal tokensDisponibles) {
		if (valor == null || valor.signum() <= 0) {
			throw new ExcepcionValidacion("El valor debe ser positivo");
		}
		if (tokensDisponibles == null || tokensDisponibles.signum() < 0) {
			throw new ExcepcionValidacion("Los tokens disponibles no pueden ser negativos");
		}
		this.valor = valor;
		this.tokensDisponibles = tokensDisponibles;
	}
}
