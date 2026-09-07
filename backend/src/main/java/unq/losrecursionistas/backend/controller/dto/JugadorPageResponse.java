package unq.losrecursionistas.backend.controller.dto;

import java.util.List;

public record JugadorPageResponse(List<JugadorResponse> contenido, int pagina, int tamano, long total) {
}