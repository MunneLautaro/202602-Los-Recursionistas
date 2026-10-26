package unq.losrecursionistas.backend.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class OrdenTest {
    @Test
    void ejecutaParcialmenteYConservaPrecioYFecha() {
        Usuario usuario = new Usuario("comprador", "clave", 1000.0);
        Jugador jugador = new Jugador("Jugador", "DEL", null, true);
        Orden orden = Orden.crear(usuario, jugador, TipoOrden.COMPRA, 10,
                new BigDecimal("12.50"), false);

        var fecha = orden.getFechaCreacion();
        orden.ejecutar(4);

        assertThat(orden.getCantidadDisponible()).isEqualTo(6);
        assertThat(orden.getEstado()).isEqualTo(EstadoOrden.ACTIVA);
        assertThat(orden.getPrecioUnitario()).isEqualByComparingTo("12.50");
        assertThat(orden.getFechaCreacion()).isEqualTo(fecha);
    }

    @Test
    void completaCuandoSeConsumeTodaLaCantidad() {
        Orden orden = Orden.crear(new Usuario("comprador2", "clave", 100.0),
                new Jugador("Jugador", "DEL", null, true), TipoOrden.VENTA, 2,
                new BigDecimal("1.00"), true);

        orden.ejecutar(2);

        assertThat(orden.getEstado()).isEqualTo(EstadoOrden.COMPLETADA);
        assertThat(orden.getCantidadDisponible()).isZero();
    }
}
