package unq.losrecursionistas.backend.controller.dto.estrategia;

import java.time.OffsetDateTime;

public record EstrategiaActivaResponse(String estrategiaAnterior, String estrategiaNueva,
                                       String usuarioAdmin, OffsetDateTime timestampCambio) {
}
