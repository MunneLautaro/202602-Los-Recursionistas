package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.exceptions.ExcepcionRecursosInsuficientes;
import unq.losrecursionistas.backend.model.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MercadoConcurrencyTest {
    @Test
    void laTenenciaNoPermiteDosVentasSobreLosMismosTokens() {
        Usuario vendedor = new Usuario("vendedor", "clave", 0.0);
        Jugador jugador = new Jugador("Jugador", "DEL", null, true);
        TenenciaToken tenencia = TenenciaToken.builder().usuario(vendedor).jugador(jugador)
                .cantidadTotal(10).cantidadReservada(0).build();

        tenencia.reservar(7);

        assertThatThrownBy(() -> tenencia.reservar(4))
                .isInstanceOf(ExcepcionRecursosInsuficientes.class);
    }

    @Test
    void elSaldoReservadoImpideDobleGasto() {
        Usuario comprador = new Usuario("comprador", "clave", 100.0);

        comprador.reservar(100.0);

        assertThatThrownBy(() -> comprador.reservar(0.01))
                .isInstanceOf(unq.losrecursionistas.backend.exceptions.ExcepcionDominio.class);
    }
}
