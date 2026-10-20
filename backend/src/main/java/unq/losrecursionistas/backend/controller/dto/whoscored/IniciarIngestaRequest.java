package unq.losrecursionistas.backend.controller.dto.whoscored;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import unq.losrecursionistas.backend.model.EjecucionIngesta;

public record IniciarIngestaRequest(
        @NotNull(message = "alcance es obligatorio") EjecucionIngesta.Alcance alcance,
        @Positive(message = "id debe ser positivo") Long id) {
    @AssertTrue(message = "id debe ser null para COMPLETA y positivo para los demas alcances")
    public boolean alcanceValido() {
        return alcance == EjecucionIngesta.Alcance.COMPLETA ? id == null : id != null && id > 0;
    }
}
