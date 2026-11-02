package unq.losrecursionistas.backend.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class CotizacionTest {
    @Test
    void redondeaValorYConservaVentana() {
        Jugador jugador = Jugador.builder().id(1L).activo(true).build();
        Cotizacion cotizacion = Cotizacion.crear(jugador, new BigDecimal("10.126"),
                LocalDateTime.now(), "PERFORMANCE_GENERAL",
                LocalDate.of(2026, 10, 14), LocalDate.of(2026, 10, 21));

        assertThat(cotizacion.getValor()).isEqualByComparingTo("10.13");
        assertThat(cotizacion.getEstrategiaUsada()).isEqualTo("PERFORMANCE_GENERAL");
        assertThat(cotizacion.getVentanaInicio()).isBefore(cotizacion.getVentanaFin());
    }

    @Test
    void rechazaVentanaInvertida() {
        Jugador jugador = Jugador.builder().id(1L).build();
        assertThatIllegalArgumentException().isThrownBy(() -> Cotizacion.crear(
                jugador, BigDecimal.TEN, LocalDateTime.now(), "GENERAL",
                LocalDate.of(2026, 10, 22), LocalDate.of(2026, 10, 21)));
    }
}
