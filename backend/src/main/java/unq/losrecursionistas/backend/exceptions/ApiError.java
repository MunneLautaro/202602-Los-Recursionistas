package unq.losrecursionistas.backend.exceptions;

import java.util.List;

public record ApiError(String codigo, String mensaje, String correlationId, List<String> detalles) {
}