package unq.losrecursionistas.backend.controller.dto.observabilidad;

import unq.losrecursionistas.backend.model.ActividadDiariaCotizacion;
import unq.losrecursionistas.backend.model.ActividadDiariaMercado;
import unq.losrecursionistas.backend.service.interfaces.MetricasNegocio;
import unq.losrecursionistas.backend.service.interfaces.ObservabilidadService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/** Respuesta de GET /api/admin/observabilidad/negocio. */
public record ResumenNegocioResponse(int dias, LocalDate desde, LocalDate hasta, EnVivo enVivo,
                                     Actual actual, Periodo periodo, List<DiaMercado> serieMercado,
                                     List<DiaCotizacion> serieCotizacion) {

    public static ResumenNegocioResponse desde(ObservabilidadService.ResumenNegocio resumen) {
        MetricasNegocio.MetricasEnVivo vivo = resumen.enVivo();
        ObservabilidadService.TotalesPeriodo totales = resumen.periodo();
        ObservabilidadService.EstrategiaActiva estrategia = resumen.actual().estrategiaActiva();
        return new ResumenNegocioResponse(resumen.dias(), resumen.desde(), resumen.hasta(),
                new EnVivo(vivo.inicio(), vivo.rechazosSaldoInsuficiente(), vivo.recalculosExitosos(),
                        vivo.recalculosFallidos(), vivo.duracionUltimoRecalculoMs(),
                        vivo.duracionPromedioRecalculoMs(), vivo.jugadoresCotizadosUltimoRecalculo(),
                        vivo.variacionPromedioUltimoRecalculo()),
                new Actual(resumen.actual().tokensEnCirculacion(),
                        new Estrategia(estrategia.codigo(), estrategia.nombre())),
                new Periodo(totales.ordenesCompra(), totales.ordenesVenta(), totales.emparejamientos(),
                        totales.volumenCreditos(), totales.cotizacionesCalculadas(), totales.variacionPromedio()),
                resumen.serieMercado().stream().map(DiaMercado::desde).toList(),
                resumen.serieCotizacion().stream().map(DiaCotizacion::desde).toList());
    }

    public record EnVivo(OffsetDateTime inicio, long rechazosSaldoInsuficiente, long recalculosExitosos,
                         long recalculosFallidos, Long duracionUltimoRecalculoMs,
                         Long duracionPromedioRecalculoMs, Integer jugadoresCotizadosUltimoRecalculo,
                         BigDecimal variacionPromedioUltimoRecalculo) {
    }

    public record Actual(long tokensEnCirculacion, Estrategia estrategiaActiva) {
    }

    public record Estrategia(String codigo, String nombre) {
    }

    public record Periodo(long ordenesCompra, long ordenesVenta, long emparejamientos,
                          BigDecimal volumenCreditos, long cotizacionesCalculadas,
                          BigDecimal variacionPromedio) {
    }

    public record DiaMercado(LocalDate dia, long ordenesCompra, long ordenesVenta, long emparejamientos,
                             BigDecimal volumenCreditos) {
        static DiaMercado desde(ActividadDiariaMercado dia) {
            return new DiaMercado(dia.dia(), dia.ordenesCompra(), dia.ordenesVenta(), dia.emparejamientos(),
                    dia.volumenCreditos());
        }
    }

    public record DiaCotizacion(LocalDate dia, long jugadoresCotizados, BigDecimal variacionPromedio) {
        static DiaCotizacion desde(ActividadDiariaCotizacion dia) {
            return new DiaCotizacion(dia.dia(), dia.jugadoresCotizados(), dia.variacionPromedio());
        }
    }
}
