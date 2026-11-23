package unq.losrecursionistas.backend.controller.dto.estrategia;

import jakarta.validation.constraints.NotBlank;

public record CambiarEstrategiaRequest(@NotBlank(message = "La estrategia es obligatoria") String estrategia) {
}
