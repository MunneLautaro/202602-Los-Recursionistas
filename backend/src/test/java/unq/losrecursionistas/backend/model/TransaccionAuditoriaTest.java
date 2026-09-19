package unq.losrecursionistas.backend.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;

class TransaccionAuditoriaTest {

	@Test
	void conservaAutorDetalleTimestampYEstadosObligatorios() {
		Usuario autor = new Usuario("jugador1", "secreto", java.math.BigDecimal.ZERO);
		Instant timestamp = Instant.parse("2026-09-19T12:00:00Z");

		TransaccionAuditoria auditoria = new TransaccionAuditoria(autor, "Compra de tokens", timestamp,
				"saldo=100", "saldo=75");

		assertThat(auditoria.getAutor()).isSameAs(autor);
		assertThat(auditoria.getDetalle()).isEqualTo("Compra de tokens");
		assertThat(auditoria.getFechaHora()).isEqualTo(timestamp);
		assertThat(auditoria.getEstadoAnterior()).isEqualTo("saldo=100");
		assertThat(auditoria.getEstadoPosterior()).isEqualTo("saldo=75");
	}

	@Test
	void rechazaDatosObligatoriosAusentes() {
		Usuario autor = new Usuario("jugador1", "secreto", java.math.BigDecimal.ZERO);
		Instant timestamp = Instant.now();

		assertThatThrownBy(() -> new TransaccionAuditoria(null, "detalle", timestamp, "antes", "despues"))
				.isInstanceOf(ExcepcionValidacion.class);
		assertThatThrownBy(() -> new TransaccionAuditoria(autor, "", timestamp, "antes", "despues"))
				.isInstanceOf(ExcepcionValidacion.class);
		assertThatThrownBy(() -> new TransaccionAuditoria(autor, "detalle", null, "antes", "despues"))
				.isInstanceOf(ExcepcionValidacion.class);
		assertThatThrownBy(() -> new TransaccionAuditoria(autor, "detalle", timestamp, null, "despues"))
				.isInstanceOf(ExcepcionValidacion.class);
		assertThatThrownBy(() -> new TransaccionAuditoria(autor, "detalle", timestamp, "antes", null))
				.isInstanceOf(ExcepcionValidacion.class);
	}

	@Test
	void exponeLaAuditoriaComoInmutableDespuesDeCrearla() {
		TransaccionAuditoria auditoria = new TransaccionAuditoria(
				new Usuario("jugador1", "secreto", java.math.BigDecimal.ZERO), "Compra",
				Instant.now(), "antes", "despues");

		assertThat(TransaccionAuditoria.class.getMethods())
				.noneMatch(method -> method.getName().startsWith("set"));
		assertThat(auditoria.getDetalle()).isEqualTo("Compra");
	}
}