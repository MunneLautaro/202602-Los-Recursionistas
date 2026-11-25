package unq.losrecursionistas.backend.controller.dto.observabilidad;

import unq.losrecursionistas.backend.service.interfaces.ObservabilidadService;

import java.time.OffsetDateTime;
import java.util.List;

/** Respuesta de GET /api/admin/observabilidad/tecnico. La salud no incluye detalles de componentes. */
public record ResumenTecnicoResponse(OffsetDateTime instante, Salud salud, Requests requests, Jvm jvm,
                                     Pool poolConexiones) {

    public static ResumenTecnicoResponse desde(ObservabilidadService.ResumenTecnico resumen) {
        var salud = resumen.salud();
        var requests = resumen.requests();
        var jvm = resumen.jvm();
        var pool = resumen.poolConexiones();
        return new ResumenTecnicoResponse(resumen.instante(),
                new Salud(salud.estado(), salud.componentes().stream()
                        .map(c -> new Componente(c.nombre(), c.estado())).toList()),
                new Requests(requests.total(), requests.latenciaMediaMs(), requests.tasaErrorCliente(),
                        requests.tasaErrorServidor(), requests.endpoints().stream()
                        .map(e -> new Endpoint(e.metodo(), e.uri(), e.cantidad(), e.erroresCliente(),
                                e.erroresServidor(), e.latenciaMediaMs())).toList()),
                new Jvm(jvm.memoriaHeapUsadaBytes(), jvm.memoriaHeapMaximaBytes(), jvm.threadsVivos(),
                        jvm.cpuProceso(), jvm.cpuSistema(), jvm.uptimeSegundos()),
                new Pool(pool.activas(), pool.inactivas(), pool.pendientes(), pool.maximas()));
    }

    public record Salud(String estado, List<Componente> componentes) {
    }

    public record Componente(String nombre, String estado) {
    }

    public record Requests(long total, Double latenciaMediaMs, Double tasaErrorCliente, Double tasaErrorServidor,
                           List<Endpoint> endpoints) {
    }

    public record Endpoint(String metodo, String uri, long cantidad, long erroresCliente, long erroresServidor,
                           Double latenciaMediaMs) {
    }

    public record Jvm(Double memoriaHeapUsadaBytes, Double memoriaHeapMaximaBytes, Double threadsVivos,
                      Double cpuProceso, Double cpuSistema, Double uptimeSegundos) {
    }

    public record Pool(Double activas, Double inactivas, Double pendientes, Double maximas) {
    }
}
