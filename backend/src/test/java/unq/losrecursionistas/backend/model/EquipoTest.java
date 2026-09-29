package unq.losrecursionistas.backend.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class EquipoTest {

    @Test
    @DisplayName("Caso Feliz: Constructor parametrizado crea una instancia de Equipo correctamente")
    void crearEquipoConConstructorParametrizado() {
        Equipo equipo = new Equipo(
                "Futbol Club Barcelona",
                "Barcelona",
                "FCB",
                "http://escudo.png",
                1899,
                "Azul y Grana",
                "Camp Nou"
        );

        assertThat(equipo.getNombre()).isEqualTo("Futbol Club Barcelona");
        assertThat(equipo.getNombreCorto()).isEqualTo("Barcelona");
        assertThat(equipo.getSigla()).isEqualTo("FCB");
        assertThat(equipo.getEscudoUrl()).isEqualTo("http://escudo.png");
        assertThat(equipo.getFundacion()).isEqualTo(1899);
        assertThat(equipo.getColores()).isEqualTo("Azul y Grana");
        assertThat(equipo.getEstadio()).isEqualTo("Camp Nou");
        // El constructor parametrizado no inicializa jugadores (solo @Builder.Default lo hace)
        assertThat(equipo.getJugadores()).isNull();
    }

    @Test
    @DisplayName("Caso Feliz: Builder crea una instancia de Equipo con Liga y Jugadores")
    void crearEquipoConBuilder() {
        Liga liga = Liga.builder().id(1L).nombre("La Liga").codigo("PD").build();
        Jugador jugador = Jugador.builder().nombre("Messi").build();

        Equipo equipo = Equipo.builder()
                .id(10L)
                .idExterno(64L)
                .nombre("FC Barcelona")
                .nombreCorto("Barca")
                .sigla("FCB")
                .escudoUrl("http://escudo.png")
                .fundacion(1899)
                .colores("Blaugrana")
                .estadio("Spotify Camp Nou")
                .liga(liga)
                .build();

        equipo.getJugadores().add(jugador);
        jugador.setEquipo(equipo);

        assertThat(equipo.getId()).isEqualTo(10L);
        assertThat(equipo.getIdExterno()).isEqualTo(64L);
        assertThat(equipo.getNombre()).isEqualTo("FC Barcelona");
        assertThat(equipo.getLiga()).isEqualTo(liga);
        assertThat(equipo.getJugadores()).containsExactly(jugador);
    }

    @Test
    @DisplayName("Caso Borde: Getters y Setters de Equipo funcionan correctamente")
    void testSettersYGetters() {
        Equipo equipo = new Equipo();
        equipo.setId(1L);
        equipo.setIdExterno(100L);
        equipo.setNombre("Real Madrid");
        equipo.setNombreCorto("Real");
        equipo.setSigla("RMA");
        equipo.setEscudoUrl("http://rm.png");
        equipo.setFundacion(1902);
        equipo.setColores("Blanco");
        equipo.setEstadio("Bernabeu");
        equipo.setJugadores(new ArrayList<>());

        assertThat(equipo.getId()).isEqualTo(1L);
        assertThat(equipo.getIdExterno()).isEqualTo(100L);
        assertThat(equipo.getNombre()).isEqualTo("Real Madrid");
        assertThat(equipo.getNombreCorto()).isEqualTo("Real");
        assertThat(equipo.getSigla()).isEqualTo("RMA");
        assertThat(equipo.getEscudoUrl()).isEqualTo("http://rm.png");
        assertThat(equipo.getFundacion()).isEqualTo(1902);
        assertThat(equipo.getColores()).isEqualTo("Blanco");
        assertThat(equipo.getEstadio()).isEqualTo("Bernabeu");
        assertThat(equipo.getJugadores()).isNotNull();
    }
}
