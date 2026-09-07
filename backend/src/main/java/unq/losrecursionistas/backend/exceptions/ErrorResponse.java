package unq.losrecursionistas.backend.exceptions;

import java.util.List;

public record ErrorResponse(String codigo, String mensaje, String correlationId, List<String> detalles) {
}