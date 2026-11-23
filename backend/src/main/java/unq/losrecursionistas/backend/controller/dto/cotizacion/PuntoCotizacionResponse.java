package unq.losrecursionistas.backend.controller.dto.cotizacion;

import unq.losrecursionistas.backend.model.Cotizacion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PuntoCotizacionResponse(LocalDateTime fecha, BigDecimal valor, String estrategiaUsada) {
    public static PuntoCotizacionResponse desde(Cotizacion cotizacion) {
        return desde(cotizacion, cotizacion.getEstrategiaUsada());
    }

    public static PuntoCotizacionResponse desde(Cotizacion cotizacion, String estrategiaLegible) {
        return new PuntoCotizacionResponse(cotizacion.getTimestampCalculo(), cotizacion.getValor(),
                estrategiaLegible);
    }
}
