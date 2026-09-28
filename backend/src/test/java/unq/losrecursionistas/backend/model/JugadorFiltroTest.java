package unq.losrecursionistas.backend.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JugadorFiltroTest {

    @Test
    @DisplayName("Caso Feliz: Creación y verificación de campos de JugadorFiltro")
    void testJugadorFiltro() {
        JugadorFiltro filtro = JugadorFiltro.builder()
                .nombre("Messi")
                .posicion("Delantero")
                .ligaId(1L)
                .equipoId(10L)
                .build();

        assertThat(filtro.getNombre()).isEqualTo("Messi");
        assertThat(filtro.getPosicion()).isEqualTo("Delantero");
        assertThat(filtro.getLigaId()).isEqualTo(1L);
        assertThat(filtro.getEquipoId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Caso Borde: Constructor vacío y getters/setters")
    void testJugadorFiltroNoArgs() {
        JugadorFiltro filtro = new JugadorFiltro();
        filtro.setNombre("Ronaldo");
        filtro.setPosicion("Centrocampista");

        assertThat(filtro.getNombre()).isEqualTo("Ronaldo");
        assertThat(filtro.getPosicion()).isEqualTo("Centrocampista");
        assertThat(filtro.getLigaId()).isNull();
        assertThat(filtro.getEquipoId()).isNull();
    }
}
