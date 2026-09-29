package unq.losrecursionistas.backend.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import unq.losrecursionistas.backend.exceptions.ExcepcionDominio;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UsuarioTest {

    @Test
    @DisplayName("Caso Feliz: Constructor crea un usuario válido con saldo inicial")
    void crearUsuarioValidoExitosamente() {
        Usuario usuario = new Usuario("jugador1", "secreto123", 100.00);

        assertThat(usuario.getNombreUsuario()).isEqualTo("jugador1");
        assertThat(usuario.getContrasena()).isEqualTo("secreto123");
        assertThat(usuario.getSaldo()).isEqualTo(100.00);
        assertThat(usuario.isHabilitado()).isTrue();
        assertThat(usuario.getFechaCreacion()).isNotNull();
        assertThat(usuario.getAutoridades()).contains("ROLE_USUARIO");
    }

    @Test
    @DisplayName("Caso Borde: Constructor permite crear un usuario con saldo 0")
    void crearUsuarioConSaldoCero() {
        Usuario usuario = new Usuario("jugador1", "secreto123", 0.0);

        assertThat(usuario.getSaldo()).isEqualTo(0.0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Caso No Feliz: Constructor lanza excepción si el nombre de usuario está vacío o en blanco")
    void crearUsuarioConNombreUsuarioVacioLanzaExcepcion(String nombreInvalido) {
        assertThatThrownBy(() -> new Usuario(nombreInvalido, "secreto123", 100.00))
                .isInstanceOf(ExcepcionValidacion.class)
                .hasMessage("El nombre de usuario es obligatorio");
    }

    @Test
    @DisplayName("Caso No Feliz: Constructor lanza excepción si el nombre de usuario es null")
    void crearUsuarioConNombreUsuarioNullLanzaExcepcion() {
        assertThatThrownBy(() -> new Usuario(null, "secreto123", 100.00))
                .isInstanceOf(ExcepcionValidacion.class)
                .hasMessage("El nombre de usuario es obligatorio");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Caso No Feliz: Constructor lanza excepción si la contraseña está vacía o en blanco")
    void crearUsuarioConContrasenaVaciaLanzaExcepcion(String contrasenaInvalida) {
        assertThatThrownBy(() -> new Usuario("jugador1", contrasenaInvalida, 100.00))
                .isInstanceOf(ExcepcionValidacion.class)
                .hasMessage("La contrasena es obligatoria");
    }

    @Test
    @DisplayName("Caso No Feliz: Constructor lanza excepción si la contraseña es null")
    void crearUsuarioConContrasenaNullLanzaExcepcion() {
        assertThatThrownBy(() -> new Usuario("jugador1", null, 100.00))
                .isInstanceOf(ExcepcionValidacion.class)
                .hasMessage("La contrasena es obligatoria");
    }

    @Test
    @DisplayName("Caso No Feliz: Constructor lanza excepción si el saldo es null")
    void crearUsuarioConSaldoNullLanzaExcepcion() {
        assertThatThrownBy(() -> new Usuario("jugador1", "secreto123", null))
                .isInstanceOf(ExcepcionValidacion.class)
                .hasMessage("El saldo no puede ser negativo");
    }

    @Test
    @DisplayName("Caso No Feliz: Constructor lanza excepción si el saldo es negativo")
    void crearUsuarioConSaldoNegativoLanzaExcepcion() {
        assertThatThrownBy(() -> new Usuario("jugador1", "secreto123", -50.0))
                .isInstanceOf(ExcepcionValidacion.class)
                .hasMessage("El saldo no puede ser negativo");
    }

    @Test
    @DisplayName("Caso Feliz: Permite debitar un monto disponible correctamente")
    void permiteDebitarUnMontoDisponible() {
        Usuario usuario = new Usuario("jugador1", "secreto", 100.00);

        usuario.debitar(35.50);

        assertEquals(64.50, usuario.getSaldo());
    }

    @Test
    @DisplayName("Caso Borde: Permite debitar exactamente el saldo total quedando en 0")
    void debitarSaldoExactoSaldoQuedaEnCero() {
        Usuario usuario = new Usuario("jugador1", "secreto", 100.00);

        usuario.debitar(100.00);

        assertEquals(0.0, usuario.getSaldo());
    }

    @Test
    @DisplayName("Caso No Feliz: Debitar con monto null lanza excepción de validación")
    void debitarMontoNullLanzaExcepcion() {
        Usuario usuario = new Usuario("jugador1", "secreto", 100.00);

        assertThatThrownBy(() -> usuario.debitar(null))
                .isInstanceOf(ExcepcionValidacion.class)
                .hasMessage("El monto debe ser positivo");
    }

    @Test
    @DisplayName("Caso No Feliz / Borde: Debitar 0 lanza excepción de validación")
    void debitarMontoCeroLanzaExcepcion() {
        Usuario usuario = new Usuario("jugador1", "secreto", 100.00);

        assertThatThrownBy(() -> usuario.debitar(0.0))
                .isInstanceOf(ExcepcionValidacion.class)
                .hasMessage("El monto debe ser positivo");
    }

    @Test
    @DisplayName("Caso No Feliz: Debitar monto negativo lanza excepción de validación")
    void debitarMontoNegativoLanzaExcepcion() {
        Usuario usuario = new Usuario("jugador1", "secreto", 100.00);

        assertThatThrownBy(() -> usuario.debitar(-10.0))
                .isInstanceOf(ExcepcionValidacion.class)
                .hasMessage("El monto debe ser positivo");
    }

    @Test
    @DisplayName("Caso No Feliz: Rechaza debitar un monto mayor al saldo disponible")
    void rechazaDebitarUnMontoMayorAlSaldo() {
        Usuario usuario = new Usuario("jugador1", "secreto", 10.00);

        assertThatThrownBy(() -> usuario.debitar(10.01))
                .isInstanceOf(ExcepcionDominio.class)
                .hasMessage("Saldo insuficiente");
    }

    @Test
    @DisplayName("Caso Feliz: Permite acreditar un monto válido al saldo")
    void permiteAcreditarUnMonto() {
        Usuario usuario = new Usuario("jugador1", "secreto", 100.00);

        usuario.acreditar(50.00);

        assertEquals(150.00, usuario.getSaldo());
    }

    @Test
    @DisplayName("Caso No Feliz: Acreditar monto null lanza excepción")
    void acreditarMontoNullLanzaExcepcion() {
        Usuario usuario = new Usuario("jugador1", "secreto", 100.00);

        assertThatThrownBy(() -> usuario.acreditar(null))
                .isInstanceOf(ExcepcionValidacion.class)
                .hasMessage("El monto debe ser positivo");
    }

    @Test
    @DisplayName("Caso No Feliz / Borde: Acreditar monto cero lanza excepción")
    void acreditarMontoCeroLanzaExcepcion() {
        Usuario usuario = new Usuario("jugador1", "secreto", 100.00);

        assertThatThrownBy(() -> usuario.acreditar(0.0))
                .isInstanceOf(ExcepcionValidacion.class)
                .hasMessage("El monto debe ser positivo");
    }

    @Test
    @DisplayName("Caso No Feliz: Acreditar monto negativo lanza excepción")
    void acreditarMontoNegativoLanzaExcepcion() {
        Usuario usuario = new Usuario("jugador1", "secreto", 100.00);

        assertThatThrownBy(() -> usuario.acreditar(-5.0))
                .isInstanceOf(ExcepcionValidacion.class)
                .hasMessage("El monto debe ser positivo");
    }

    @Test
    @DisplayName("Caso Feliz: Permite deshabilitar y habilitar el usuario")
    void permiteCambiarEstadoHabilitado() {
        Usuario usuario = new Usuario("jugador1", "secreto", 100.00);

        usuario.deshabilitar();
        assertThat(usuario.isHabilitado()).isFalse();

        usuario.habilitar();
        assertThat(usuario.isHabilitado()).isTrue();
    }

    @Test
    @DisplayName("Caso Feliz: Setters y Getters funcionan correctamente")
    void testSettersYGetters() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombreUsuario("testUser");
        usuario.setContrasena("pass");
        usuario.setSaldo(500.0);

        assertThat(usuario.getId()).isEqualTo(1L);
        assertThat(usuario.getNombreUsuario()).isEqualTo("testUser");
        assertThat(usuario.getContrasena()).isEqualTo("pass");
        assertThat(usuario.getSaldo()).isEqualTo(500.0);
    }
}