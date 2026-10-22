package unq.losrecursionistas.backend.controller.dto.jugador;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.model.Equipo;
import unq.losrecursionistas.backend.model.EstadisticaTemporada;
import unq.losrecursionistas.backend.model.Jugador;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class EstadisticaTemporadaResponseDTOTest {

    @Test
    void desdeModeloExponeSoloDatosEscalaresDeJugadorEquipoYEstadistica() {
        Equipo equipo = Equipo.builder().id(10L).nombre("River Plate").build();
        Jugador jugador = Jugador.builder()
                .id(160L)
                .nombre("Jugador de prueba")
                .posicion("Delantero")
                .equipo(equipo)
                .build();
        EstadisticaTemporada estadistica = EstadisticaTemporada.builder()
                .id(1L)
                .jugador(jugador)
                .torneo("Liga Profesional")
                .goles(8)
                .ratingPromedio(new BigDecimal("7.50"))
                .build();

        EstadisticaTemporadaResponseDTO resultado =
                EstadisticaTemporadaResponseDTO.desdeModelo(estadistica);

        assertThat(resultado.jugadorId()).isEqualTo(160L);
        assertThat(resultado.jugadorNombre()).isEqualTo("Jugador de prueba");
        assertThat(resultado.equipoNombre()).isEqualTo("River Plate");
        assertThat(resultado.goles()).isEqualTo(8);
        assertThat(resultado.ratingPromedio()).isEqualByComparingTo("7.50");
    }
}
