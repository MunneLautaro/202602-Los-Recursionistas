package unq.losrecursionistas.backend.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LigaTest {

    @Test
    @DisplayName("Caso Feliz: Constructor Liga(nombre, codigo) crea la entidad con fecha de creación")
    void crearLigaConNombreYCodigoValidos() {
        Liga liga = new Liga("Premier League", "PL");

        assertThat(liga.getNombre()).isEqualTo("Premier League");
        assertThat(liga.getCodigo()).isEqualTo("PL");
        assertThat(liga.getFechaCreacion()).isNotNull();
    }

    @Test
    @DisplayName("Caso Feliz: Constructor Liga(idExterno, nombre, codigo) crea la entidad correctamente")
    void crearLigaConIdExternoNombreYCodigoValidos() {
        Liga liga = new Liga(2021L, "Premier League", "PL");

        assertThat(liga.getIdExterno()).isEqualTo(2021L);
        assertThat(liga.getNombre()).isEqualTo("Premier League");
        assertThat(liga.getCodigo()).isEqualTo("PL");
        assertThat(liga.getFechaCreacion()).isNotNull();
    }

    @Test
    @DisplayName("Caso No Feliz: Constructor lanza excepción si el nombre es null")
    void crearLigaConNombreNullLanzaExcepcion() {
        assertThatThrownBy(() -> new Liga(null, "PL"))
                .isInstanceOf(ExcepcionValidacion.class)
                .hasMessage("El nombre de la liga es obligatorio");

        assertThatThrownBy(() -> new Liga(2021L, null, "PL"))
                .isInstanceOf(ExcepcionValidacion.class)
                .hasMessage("El nombre de la liga es obligatorio");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Caso No Feliz / Borde: Constructor lanza excepción si el nombre está vacío o en blanco")
    void crearLigaConNombreVacioLanzaExcepcion(String nombreInvalido) {
        assertThatThrownBy(() -> new Liga(nombreInvalido, "PL"))
                .isInstanceOf(ExcepcionValidacion.class)
                .hasMessage("El nombre de la liga es obligatorio");

        assertThatThrownBy(() -> new Liga(2021L, nombreInvalido, "PL"))
                .isInstanceOf(ExcepcionValidacion.class)
                .hasMessage("El nombre de la liga es obligatorio");
    }

    @Test
    @DisplayName("Caso No Feliz: Constructor lanza excepción si el código es null")
    void crearLigaConCodigoNullLanzaExcepcion() {
        assertThatThrownBy(() -> new Liga("Premier League", null))
                .isInstanceOf(ExcepcionValidacion.class)
                .hasMessage("El codigo de la liga es obligatorio");

        assertThatThrownBy(() -> new Liga(2021L, "Premier League", null))
                .isInstanceOf(ExcepcionValidacion.class)
                .hasMessage("El codigo de la liga es obligatorio");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Caso No Feliz / Borde: Constructor lanza excepción si el código está vacío o en blanco")
    void crearLigaConCodigoVacioLanzaExcepcion(String codigoInvalido) {
        assertThatThrownBy(() -> new Liga("Premier League", codigoInvalido))
                .isInstanceOf(ExcepcionValidacion.class)
                .hasMessage("El codigo de la liga es obligatorio");

        assertThatThrownBy(() -> new Liga(2021L, "Premier League", codigoInvalido))
                .isInstanceOf(ExcepcionValidacion.class)
                .hasMessage("El codigo de la liga es obligatorio");
    }

    @Test
    @DisplayName("Caso Feliz: Builder y Getters/Setters de Liga")
    void testBuilderYSettersGetters() {
        LocalDate ahora = LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires"));
        Liga liga = Liga.builder()
                .id(1L)
                .idExterno(2021L)
                .nombre("La Liga")
                .codigo("PD")
                .fechaCreacion(ahora)
                .build();

        assertThat(liga.getId()).isEqualTo(1L);
        assertThat(liga.getIdExterno()).isEqualTo(2021L);
        assertThat(liga.getNombre()).isEqualTo("La Liga");
        assertThat(liga.getCodigo()).isEqualTo("PD");
        assertThat(liga.getFechaCreacion()).isEqualTo(ahora);

        liga.setNombre("Serie A");
        liga.setCodigo("SA");
        assertThat(liga.getNombre()).isEqualTo("Serie A");
        assertThat(liga.getCodigo()).isEqualTo("SA");
    }
}
