package unq.losrecursionistas.backend.controller.dto;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.controller.dto.cotizacion.CotizacionResponse;
import unq.losrecursionistas.backend.model.Cotizacion;
import unq.losrecursionistas.backend.model.Jugador;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CotizacionResponseTest {

    @Test
    void convierteCotizacionADto() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 1, 1, 10, 0);
        Cotizacion cotizacion = Cotizacion.crear(Jugador.builder().id(3L).build(), BigDecimal.TEN,
                timestamp, "GENERAL", LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 31));

        CotizacionResponse response = CotizacionResponse.desde(cotizacion);

        assertThat(response.jugadorId()).isEqualTo(3L);
        assertThat(response.valor()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(response.timestampCalculo()).isEqualTo(timestamp);
    }
}
