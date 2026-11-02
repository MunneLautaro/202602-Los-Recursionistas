package unq.losrecursionistas.backend.model;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.exceptions.ExcepcionRecursosInsuficientes;

import static org.assertj.core.api.Assertions.*;

class TenenciaTokenTest {
    @Test
    void reservaLiberaYAcreditaTokensSinRomperInvariantes() {
        TenenciaToken tenencia = TenenciaToken.builder()
                .cantidadTotal(10).cantidadReservada(0).build();

        tenencia.reservar(4);
        assertThat(tenencia.cantidadDisponible()).isEqualTo(6);
        tenencia.liberar(2);
        tenencia.debitar(2);
        tenencia.acreditar(5);

        assertThat(tenencia.getCantidadTotal()).isEqualTo(13);
        assertThat(tenencia.getCantidadReservada()).isEqualTo(2);
        assertThat(tenencia.cantidadDisponible()).isEqualTo(11);
    }

    @Test
    void rechazaReservarMasTokensDisponibles() {
        TenenciaToken tenencia = TenenciaToken.builder()
                .cantidadTotal(2).cantidadReservada(1).build();

        assertThatThrownBy(() -> tenencia.reservar(2))
                .isInstanceOf(ExcepcionRecursosInsuficientes.class);
    }
}
