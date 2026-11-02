package unq.losrecursionistas.backend.service.interfaces;

public record ResultadoIngesta(int jugadoresProcesados, int partidosInsertados, int partidosActualizados,
                               int errores) {
}
