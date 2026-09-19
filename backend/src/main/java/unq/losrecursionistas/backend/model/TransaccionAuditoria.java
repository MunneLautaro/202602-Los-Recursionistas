package unq.losrecursionistas.backend.model;

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
@Table(name = "transacciones_auditoria")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class TransaccionAuditoria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario autor;

	@Column(nullable = false)
	private String detalle;

	@Column(name = "fecha_hora", nullable = false)
	private Instant fechaHora;

	@Column(name = "estado_anterior", nullable = false, columnDefinition = "TEXT")
	private String estadoAnterior;

	@Column(name = "estado_posterior", nullable = false, columnDefinition = "TEXT")
	private String estadoPosterior;

	public TransaccionAuditoria(Usuario autor, String detalle, Instant fechaHora, String estadoAnterior,
			String estadoPosterior) {
		validar(autor, detalle, fechaHora, estadoAnterior, estadoPosterior);
		this.autor = autor;
		this.detalle = detalle;
		this.fechaHora = fechaHora;
		this.estadoAnterior = estadoAnterior;
		this.estadoPosterior = estadoPosterior;
	}

	private static void validar(Usuario autor, String detalle, Instant fechaHora, String estadoAnterior,
			String estadoPosterior) {
		if (autor == null || detalle == null || detalle.isBlank() || fechaHora == null
				|| estadoAnterior == null || estadoAnterior.isBlank() || estadoPosterior == null
				|| estadoPosterior.isBlank()) {
			throw new ExcepcionValidacion("Los datos de auditoria son obligatorios");
		}
	}
}
