package unq.losrecursionistas.backend.exceptions;

import java.time.Instant;

public record RespuestaError(int codigo, String mensaje, String detalle, Instant timestamp) {
}