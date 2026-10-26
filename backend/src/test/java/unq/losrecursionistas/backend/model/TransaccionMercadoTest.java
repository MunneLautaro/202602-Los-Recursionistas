package unq.losrecursionistas.backend.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class TransaccionMercadoTest {
    @Test
    void creaTransaccionConSnapshotDePrecioYTotal() {
        Usuario comprador = new Usuario("comprador3", "clave", 1000.0);
        Usuario vendedor = new Usuario("vendedor3", "clave", 0.0);
        Jugador jugador = new Jugador("Jugador", "DEL", null, true);
        Orden compra = Orden.crear(comprador, jugador, TipoOrden.COMPRA, 5,
                new BigDecimal("10.00"), false);
        Orden venta = Orden.crear(vendedor, jugador, TipoOrden.VENTA, 5,
                new BigDecimal("10.00"), false);

        TransaccionMercado transaccion = TransaccionMercado.crear(compra, venta, 3);

        assertThat(transaccion.getCantidad()).isEqualTo(3);
        assertThat(transaccion.getPrecioUnitario()).isEqualByComparingTo("10.00");
        assertThat(transaccion.getTotal()).isEqualByComparingTo("30.00");
        assertThat(transaccion.getEjecutadaEn().getOffset().getId()).isEqualTo("-03:00");
    }

    @Test
    void auditoriaExponeSnapshotsSinOperacionesDeMutacion() {
        Usuario autor = new Usuario("auditor3", "clave", 0.0);
        AuditoriaTransaccional auditoria = AuditoriaTransaccional.crear(
                "ORDEN", 1L, autor, "match", "{}", "{\"estado\":\"ACTIVA\"}");

        assertThat(auditoria.getEstadoAnterior()).isEqualTo("{}");
        assertThat(auditoria.getEstadoPosterior()).contains("ACTIVA");
        assertThat(auditoria.getEjecutadaEn()).isNotNull();
    }
}
