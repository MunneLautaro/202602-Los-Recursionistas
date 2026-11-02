package unq.losrecursionistas.backend.service;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.controller.dto.orden.OrdenRequest;
import unq.losrecursionistas.backend.controller.dto.usuario.RecargaCreditosRequest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MercadoContratoTest {
    @Test
    void orderRequestHasPositiveIntegerContract() {
        var parameters = OrdenRequest.class.getDeclaredConstructors()[0].getParameters();
        assertThat(parameters).hasSize(2);
        assertThat(parameters[0].getAnnotation(NotNull.class)).isNotNull();
        assertThat(parameters[0].getAnnotation(Positive.class)).isNotNull();
        assertThat(parameters[1].getType()).isEqualTo(Integer.class);
        assertThat(parameters[1].getAnnotation(NotNull.class)).isNotNull();
        assertThat(parameters[1].getAnnotation(Positive.class)).isNotNull();
    }

    @Test
    void rechargeRequestRequiresPositiveAmount() {
        var amount = RecargaCreditosRequest.class.getDeclaredConstructors()[0].getParameters()[0];
        assertThat(amount.getType()).isEqualTo(BigDecimal.class);
        assertThat(amount.getAnnotation(NotNull.class)).isNotNull();
        assertThat(amount.getAnnotation(Positive.class)).isNotNull();
    }
}
