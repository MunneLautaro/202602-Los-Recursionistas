package unq.losrecursionistas.backend.controller.dto.usuario;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record RecargaCreditosRequest(@NotNull @Positive BigDecimal monto) {
}
