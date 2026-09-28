package unq.losrecursionistas.backend.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.exceptions.ExcepcionDominio;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JugadorTest {

    private Equipo equipo1;
    private Liga liga1;

    @BeforeEach
    void setUp() {
        liga1 = Liga.builder()
                .id(1L)
                .nombre("Liga Española")
                .codigo("ESP")
                .build();

        equipo1 = Equipo.builder()
                .id(10L)
                .nombre("FC Barcelona")
                .liga(liga1)
                .build();
    }

    @Test
    @DisplayName("Caso Feliz: Constructor parametrizado de Jugador crea la instancia correctamente")
    void crearJugadorConConstructorParametrizado() {
        Jugador jugador = new Jugador("Lionel Messi", "Delantero", equipo1, true);

        assertThat(jugador.getNombre()).isEqualTo("Lionel Messi");
        assertThat(jugador.getPosicion()).isEqualTo("Delantero");
        assertThat(jugador.getEquipo()).isEqualTo(equipo1);
        assertThat(jugador.isActivo()).isTrue();
    }

    @Test
    @DisplayName("Caso Feliz: Builder de Jugador construye la entidad correctamente")
    void crearJugadorConBuilder() {
        LocalDate fechaNac = LocalDate.of(1987, 6, 24);
        Jugador jugador = Jugador.builder()
                .id(100L)
                .nombre("Lionel Messi")
                .posicion("Delantero")
                .idExterno(10L)
                .nacionalidad("Argentina")
                .fechaNacimiento(fechaNac)
                .equipo(equipo1)
                .activo(true)
                .build();

        assertThat(jugador.getId()).isEqualTo(100L);
        assertThat(jugador.getNombre()).isEqualTo("Lionel Messi");
        assertThat(jugador.getPosicion()).isEqualTo("Delantero");
        assertThat(jugador.getIdExterno()).isEqualTo(10L);
        assertThat(jugador.getNacionalidad()).isEqualTo("Argentina");
        assertThat(jugador.getFechaNacimiento()).isEqualTo(fechaNac);
        assertThat(jugador.getEquipo()).isEqualTo(equipo1);
        assertThat(jugador.isActivo()).isTrue();
    }

    @Test
    @DisplayName("Caso Feliz: validarActivo no lanza excepción cuando el jugador está activo")
    void validarActivoJugadorActivoNoLanzaExcepcion() {
        Jugador jugador = Jugador.builder()
                .nombre("Lionel Messi")
                .activo(true)
                .build();

        assertDoesNotThrow(jugador::validarActivo);
    }

    @Test
    @DisplayName("Caso No Feliz: validarActivo lanza ExcepcionDominio cuando el jugador no está activo")
    void validarActivoJugadorInactivoLanzaExcepcion() {
        Jugador jugador = Jugador.builder()
                .nombre("Jugador Inactivo")
                .activo(false)
                .build();

        assertThatThrownBy(jugador::validarActivo)
                .isInstanceOf(ExcepcionDominio.class)
                .hasMessage("El jugador no esta activo");
    }

    @Test
    @DisplayName("Caso Borde: Setters y Getters permiten modificar el estado del jugador")
    void testSettersYGetters() {
        Jugador jugador = new Jugador();
        jugador.setId(5L);
        jugador.setNombre("Pedri");
        jugador.setPosicion("Centrocampista");
        jugador.setIdExterno(200L);
        jugador.setNacionalidad("España");
        jugador.setActivo(false);

        assertThat(jugador.getId()).isEqualTo(5L);
        assertThat(jugador.getNombre()).isEqualTo("Pedri");
        assertThat(jugador.getPosicion()).isEqualTo("Centrocampista");
        assertThat(jugador.getIdExterno()).isEqualTo(200L);
        assertThat(jugador.getNacionalidad()).isEqualTo("España");
        assertThat(jugador.isActivo()).isFalse();
    }
}
