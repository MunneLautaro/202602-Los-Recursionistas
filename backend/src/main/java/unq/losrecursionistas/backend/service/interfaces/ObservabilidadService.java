package unq.losrecursionistas.backend.service.interfaces;

import unq.losrecursionistas.backend.model.ActividadDiariaCotizacion;
import unq.losrecursionistas.backend.model.ActividadDiariaMercado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Casos de uso del panel de observabilidad para administradores: resumen de
 * negocio (metricas en vivo y series diarias persistidas) y resumen tecnico.
 */
public interface ObservabilidadService {

    /** Resumen de negocio para un periodo de {@code dias} dias (7, 30 o 90, validado por el controller). */
    ResumenNegocio resumenNegocio(int dias);

    /** Instantanea tecnica: salud sin detalles, requests, JVM y pool de conexiones. */
    ResumenTecnico resumenTecnico();

    record ResumenNegocio(int dias, LocalDate desde, LocalDate hasta,
                          MetricasNegocio.MetricasEnVivo enVivo, Actual actual, TotalesPeriodo periodo,
                          List<ActividadDiariaMercado> serieMercado,
                          List<ActividadDiariaCotizacion> serieCotizacion) {
    }

    record Actual(long tokensEnCirculacion, EstrategiaActiva estrategiaActiva) {
    }

    record EstrategiaActiva(String codigo, String nombre) {
    }

    record TotalesPeriodo(long ordenesCompra, long ordenesVenta, long emparejamientos,
                          BigDecimal volumenCreditos, long cotizacionesCalculadas,
                          BigDecimal variacionPromedio) {
    }

    record ResumenTecnico(OffsetDateTime instante, Salud salud, ResumenRequests requests, Jvm jvm,
                          PoolConexiones poolConexiones) {
    }

    record Salud(String estado, List<ComponenteSalud> componentes) {
    }

    record ComponenteSalud(String nombre, String estado) {
    }

    record ResumenRequests(long total, Double latenciaMediaMs, Double tasaErrorCliente,
                           Double tasaErrorServidor, List<EndpointRequests> endpoints) {
    }

    record EndpointRequests(String metodo, String uri, long cantidad, long erroresCliente,
                            long erroresServidor, Double latenciaMediaMs) {
    }

    record Jvm(Double memoriaHeapUsadaBytes, Double memoriaHeapMaximaBytes, Double threadsVivos,
               Double cpuProceso, Double cpuSistema, Double uptimeSegundos) {
    }

    record PoolConexiones(Double activas, Double inactivas, Double pendientes, Double maximas) {
    }
}
