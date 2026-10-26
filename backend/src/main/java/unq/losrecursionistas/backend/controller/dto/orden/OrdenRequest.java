package unq.losrecursionistas.backend.controller.dto.orden;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrdenRequest(
        @NotNull @Positive Long jugadorId,
        @NotNull @Positive Integer cantidad) {
}
