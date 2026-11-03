package unq.losrecursionistas.backend.model;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CasoVinculacionTest {

    @Test
    void construyeCasoConBuilder() {
        CasoVinculacion caso = CasoVinculacion.builder()
                .idWhoscored(10L)
                .nombre("Jugador")
                .motivo(CasoVinculacion.Motivo.AMBIGUO)
                .estado(CasoVinculacion.Estado.PENDIENTE)
                .creadoEn(OffsetDateTime.now())
                .build();

        assertThat(caso.getIdWhoscored()).isEqualTo(10L);
        assertThat(caso.getEstado()).isEqualTo(CasoVinculacion.Estado.PENDIENTE);
    }
}
