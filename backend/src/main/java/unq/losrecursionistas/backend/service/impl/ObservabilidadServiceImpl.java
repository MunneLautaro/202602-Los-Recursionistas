package unq.losrecursionistas.backend.service.impl;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.TimeGauge;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unq.losrecursionistas.backend.model.ActividadDiariaCotizacion;
import unq.losrecursionistas.backend.model.ActividadDiariaMercado;
import unq.losrecursionistas.backend.service.interfaces.EstrategiaAdminService;
import unq.losrecursionistas.backend.service.interfaces.MetricasNegocio;
import unq.losrecursionistas.backend.service.interfaces.ObservabilidadService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class ObservabilidadServiceImpl implements ObservabilidadService {
    private static final ZoneId ZONA_ARGENTINA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final int MAXIMO_ENDPOINTS = 10;
    private static final List<String> PREFIJOS_EXCLUIDOS = List.of("/actuator", "/api/admin/observabilidad");
    private static final String TAG_AREA = "area";
    private static final String HEAP = "heap";

    private final MetricasNegocio metricas;
    private final EstrategiaAdminService estrategias;
    private final RepositoriosObservabilidad repositorios;
    private final MeterRegistry registro;
    private final ObjectProvider<HealthEndpoint> salud;
    private final Clock clock;

    @Autowired
    public ObservabilidadServiceImpl(MetricasNegocio metricas, EstrategiaAdminService estrategias,
                                     RepositoriosObservabilidad repositorios, MeterRegistry registro,
                                     ObjectProvider<HealthEndpoint> salud) {
        this(metricas, estrategias, repositorios, registro, salud, Clock.system(ZONA_ARGENTINA));
    }

    public ObservabilidadServiceImpl(MetricasNegocio metricas, EstrategiaAdminService estrategias,
                                     RepositoriosObservabilidad repositorios, MeterRegistry registro,
                                     ObjectProvider<HealthEndpoint> salud, Clock clock) {
        this.metricas = metricas;
        this.estrategias = estrategias;
        this.repositorios = repositorios;
        this.registro = registro;
        this.salud = salud;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenNegocio resumenNegocio(int dias) {
        LocalDate hasta = LocalDate.now(clock);
        LocalDate desde = hasta.minusDays(dias - 1L);
        List<ActividadDiariaMercado> serieMercado = serieMercado(desde, hasta);
        List<ActividadDiariaCotizacion> serieCotizacion = serieCotizacion(desde, hasta);
        TotalesPeriodo totales = new TotalesPeriodo(
                serieMercado.stream().mapToLong(ActividadDiariaMercado::ordenesCompra).sum(),
                serieMercado.stream().mapToLong(ActividadDiariaMercado::ordenesVenta).sum(),
                serieMercado.stream().mapToLong(ActividadDiariaMercado::emparejamientos).sum(),
                serieMercado.stream().map(ActividadDiariaMercado::volumenCreditos)
                        .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP),
                serieCotizacion.stream().mapToLong(ActividadDiariaCotizacion::jugadoresCotizados).sum(),
                variacionPonderada(serieCotizacion));
        return new ResumenNegocio(dias, desde, hasta, metricas.consultarEnVivo(),
                new Actual(tokensEnCirculacion(), estrategiaActiva()), totales, serieMercado, serieCotizacion);
    }

    @Override
    public ResumenTecnico resumenTecnico() {
        return new ResumenTecnico(OffsetDateTime.now(clock), resumenSalud(), resumenRequests(),
                new Jvm(sumarGauges("jvm.memory.used", TAG_AREA, HEAP),
                        sumarGauges("jvm.memory.max", TAG_AREA, HEAP),
                        sumarGauges("jvm.threads.live", null, null),
                        sumarGauges("process.cpu.usage", null, null),
                        sumarGauges("system.cpu.usage", null, null),
                        uptimeSegundos()),
                new PoolConexiones(sumarGauges("hikaricp.connections.active", null, null),
                        sumarGauges("hikaricp.connections.idle", null, null),
                        sumarGauges("hikaricp.connections.pending", null, null),
                        sumarGauges("hikaricp.connections.max", null, null)));
    }

    // --- Negocio ---------------------------------------------------------------------------

    /** Combina ordenes y transacciones por dia y completa con cero los dias sin actividad. */
    private List<ActividadDiariaMercado> serieMercado(LocalDate desde, LocalDate hasta) {
        Map<LocalDate, ActividadDiariaMercado> ordenesPorDia = indexar(
                repositorios.ordenes().contarOrdenesCreadasPorDia(desde, hasta));
        Map<LocalDate, ActividadDiariaMercado> transaccionesPorDia = indexar(
                repositorios.transacciones().resumirTransaccionesPorDia(desde, hasta));
        List<ActividadDiariaMercado> serie = new ArrayList<>();
        for (LocalDate dia = desde; !dia.isAfter(hasta); dia = dia.plusDays(1)) {
            ActividadDiariaMercado vacia = ActividadDiariaMercado.vacia(dia);
            ActividadDiariaMercado ordenes = ordenesPorDia.getOrDefault(dia, vacia);
            ActividadDiariaMercado transacciones = transaccionesPorDia.getOrDefault(dia, vacia);
            serie.add(new ActividadDiariaMercado(dia, ordenes.ordenesCompra(), ordenes.ordenesVenta(),
                    transacciones.emparejamientos(), transacciones.volumenCreditos()));
        }
        return serie;
    }

    /** Cotizaciones por dia, con dias vacios (0 y variacion nula) donde no hubo recalculos. */
    private List<ActividadDiariaCotizacion> serieCotizacion(LocalDate desde, LocalDate hasta) {
        Map<LocalDate, ActividadDiariaCotizacion> porDia = new HashMap<>();
        repositorios.cotizaciones().resumirPorDia(desde, hasta).forEach(fila -> porDia.put(fila.dia(), fila));
        List<ActividadDiariaCotizacion> serie = new ArrayList<>();
        for (LocalDate dia = desde; !dia.isAfter(hasta); dia = dia.plusDays(1)) {
            serie.add(porDia.getOrDefault(dia, ActividadDiariaCotizacion.vacia(dia)));
        }
        return serie;
    }

    /** Promedio de las variaciones diarias ponderado por la cantidad de cotizaciones con valor previo. */
    private static BigDecimal variacionPonderada(List<ActividadDiariaCotizacion> serie) {
        long totalConPrevio = 0;
        BigDecimal suma = BigDecimal.ZERO;
        for (ActividadDiariaCotizacion dia : serie) {
            if (dia.variacionPromedio() != null && dia.conPrevio() > 0) {
                suma = suma.add(dia.variacionPromedio().multiply(BigDecimal.valueOf(dia.conPrevio())));
                totalConPrevio += dia.conPrevio();
            }
        }
        return totalConPrevio == 0 ? null
                : suma.divide(BigDecimal.valueOf(totalConPrevio), 6, RoundingMode.HALF_UP);
    }

    private static Map<LocalDate, ActividadDiariaMercado> indexar(List<ActividadDiariaMercado> filas) {
        Map<LocalDate, ActividadDiariaMercado> indice = new HashMap<>();
        filas.forEach(fila -> indice.put(fila.dia(), fila));
        return indice;
    }

    private long tokensEnCirculacion() {
        return (long) MetricasNegocioMicrometer.tokensEnCirculacion(repositorios.tenencias(), repositorios.usuarios());
    }

    private EstrategiaActiva estrategiaActiva() {
        var estado = estrategias.consultar();
        return new EstrategiaActiva(estado.estrategiaActiva(), estado.nombreActivo());
    }

    // --- Tecnico ---------------------------------------------------------------------------

    /** Estado global y de cada componente, sin detalles (no expone rutas ni motor de base). */
    private Salud resumenSalud() {
        HealthEndpoint endpoint = salud.getIfAvailable();
        if (endpoint == null) {
            return new Salud("UNKNOWN", List.of());
        }
        HealthDescriptor descriptor = endpoint.health();
        List<ComponenteSalud> componentes = new ArrayList<>();
        if (descriptor instanceof CompositeHealthDescriptor compuesto && compuesto.getComponents() != null) {
            compuesto.getComponents().forEach((nombre, componente) ->
                    componentes.add(new ComponenteSalud(nombre, codigoEstado(componente))));
        }
        componentes.sort(Comparator.comparing(ComponenteSalud::nombre));
        return new Salud(codigoEstado(descriptor), componentes);
    }

    private static String codigoEstado(HealthDescriptor descriptor) {
        return descriptor == null || descriptor.getStatus() == null ? "UNKNOWN" : descriptor.getStatus().getCode();
    }

    /** Requests HTTP agrupados por metodo y URI, sin los del propio monitoreo. */
    private ResumenRequests resumenRequests() {
        Map<String, AcumuladoEndpoint> porEndpoint = new LinkedHashMap<>();
        for (Timer timer : registro.find("http.server.requests").timers()) {
            String uri = timer.getId().getTag("uri");
            if (uri == null || PREFIJOS_EXCLUIDOS.stream().anyMatch(uri::startsWith)) {
                continue;
            }
            String metodo = Objects.requireNonNullElse(timer.getId().getTag("method"), "?");
            porEndpoint.computeIfAbsent(metodo + " " + uri, clave -> new AcumuladoEndpoint(metodo, uri))
                    .sumar(timer.count(), timer.totalTime(TimeUnit.MILLISECONDS), timer.getId().getTag("outcome"));
        }
        Collection<AcumuladoEndpoint> acumulados = porEndpoint.values();
        long total = acumulados.stream().mapToLong(a -> a.cantidad).sum();
        double tiempoTotal = acumulados.stream().mapToDouble(a -> a.tiempoTotalMs).sum();
        long erroresCliente = acumulados.stream().mapToLong(a -> a.erroresCliente).sum();
        long erroresServidor = acumulados.stream().mapToLong(a -> a.erroresServidor).sum();
        List<EndpointRequests> top = acumulados.stream()
                .sorted(Comparator.comparingLong((AcumuladoEndpoint a) -> a.cantidad).reversed())
                .limit(MAXIMO_ENDPOINTS)
                .map(AcumuladoEndpoint::aEndpoint)
                .toList();
        return new ResumenRequests(total, proporcion(tiempoTotal, total), proporcion(erroresCliente, total),
                proporcion(erroresServidor, total), top);
    }

    /** Suma los gauges con ese nombre (y etiqueta opcional); null si no existe ninguno. */
    private Double sumarGauges(String nombre, String etiqueta, String valor) {
        var busqueda = registro.find(nombre);
        if (etiqueta != null) {
            busqueda = busqueda.tag(etiqueta, valor);
        }
        Collection<Gauge> gauges = busqueda.gauges();
        if (gauges.isEmpty()) {
            return null;
        }
        return gauges.stream().mapToDouble(Gauge::value)
                .filter(v -> !Double.isNaN(v) && v >= 0)
                .sum();
    }

    private Double uptimeSegundos() {
        TimeGauge uptime = registro.find("process.uptime").timeGauge();
        return uptime == null ? null : uptime.value(TimeUnit.SECONDS);
    }

    private static Double proporcion(double parte, long total) {
        return total == 0 ? null : parte / total;
    }

    /** Acumulador mutable de un endpoint mientras se recorren los timers. */
    private static final class AcumuladoEndpoint {
        private final String metodo;
        private final String uri;
        private long cantidad;
        private double tiempoTotalMs;
        private long erroresCliente;
        private long erroresServidor;

        private AcumuladoEndpoint(String metodo, String uri) {
            this.metodo = metodo;
            this.uri = uri;
        }

        private void sumar(long cantidadTimer, double tiempoMs, String outcome) {
            cantidad += cantidadTimer;
            tiempoTotalMs += tiempoMs;
            if ("CLIENT_ERROR".equals(outcome)) {
                erroresCliente += cantidadTimer;
            } else if ("SERVER_ERROR".equals(outcome)) {
                erroresServidor += cantidadTimer;
            }
        }

        private EndpointRequests aEndpoint() {
            return new EndpointRequests(metodo, uri, cantidad, erroresCliente, erroresServidor,
                    proporcion(tiempoTotalMs, cantidad));
        }
    }
}
