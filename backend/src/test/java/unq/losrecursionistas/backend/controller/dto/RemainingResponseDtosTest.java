package unq.losrecursionistas.backend.controller.dto;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.controller.dto.orden.TenenciaTokenResponse;
import unq.losrecursionistas.backend.controller.dto.orden.TransaccionResponse;
import unq.losrecursionistas.backend.model.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RemainingResponseDtosTest {

    @Test
    void convierteTenenciaYTransaccion() {
        Equipo equipo = Equipo.builder().nombre("Equipo").build();
        Jugador jugador = Jugador.builder().id(1L).nombre("Jugador").equipo(equipo).build();
        TenenciaToken tenencia = TenenciaToken.builder().id(2L).jugador(jugador)
                .cantidadTotal(10).cantidadReservada(3).build();
        assertThat(TenenciaTokenResponse.desde(tenencia).cantidadDisponible()).isEqualTo(7);

        Usuario comprador = Usuario.builder().id(3L).nombreUsuario("comprador").saldo(1D).build();
        Usuario vendedor = Usuario.builder().id(4L).nombreUsuario("vendedor").saldo(1D).build();
        TransaccionMercado tx = TransaccionMercado.builder().id(5L).jugador(jugador)
                .comprador(comprador).vendedor(vendedor).cantidad(2)
                .precioUnitario(BigDecimal.TEN).total(BigDecimal.valueOf(20))
                .ejecutadaEn(OffsetDateTime.now()).build();
        assertThat(TransaccionResponse.desde(tx).total()).isEqualByComparingTo("20");
    }
}
