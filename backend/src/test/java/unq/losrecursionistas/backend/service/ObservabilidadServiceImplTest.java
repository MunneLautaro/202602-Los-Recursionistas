package unq.losrecursionistas.backend.service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.TimeGauge;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.actuate.endpoint.IndicatedHealthDescriptor;
import org.springframework.boot.health.contributor.Status;
import unq.losrecursionistas.backend.model.ActividadDiariaCotizacion;
import unq.losrecursionistas.backend.model.ActividadDiariaMercado;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioCotizacion;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioOrden;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioTenenciaToken;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioTransaccionMercado;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioUsuario;
import unq.losrecursionistas.backend.service.impl.ObservabilidadServiceImpl;
import unq.losrecursionistas.backend.service.impl.RepositoriosObservabilidad;
import unq.losrecursionistas.backend.service.interfaces.EstrategiaAdminService;
import unq.losrecursionistas.backend.service.interfaces.MetricasNegocio;
import unq.losrecursionistas.backend.service.interfaces.ObservabilidadService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ObservabilidadServiceImplTest {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    /** 2026-11-24 22:00 en Argentina (01:00 UTC del dia siguiente). */
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-25T01:00:00Z"), ZONA);
    private static final LocalDate HOY = LocalDate.of(2026, 11, 24);
    private static final String HTTP = "http.server.requests";

    private MetricasNegocio metricas;
    private EstrategiaAdminService estrategias;
    private RepositorioOrden ordenes;
    private RepositorioTransaccionMercado transacciones;
    private RepositorioTenenciaToken tenencias;
    private RepositorioUsuario usuarios;
    private RepositorioCotizacion cotizaciones;
    private SimpleMeterRegistry registro;
    private HealthEndpoint healthEndpoint;
    private ObservabilidadServiceImpl servicio;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void configurar() {
        metricas = mock(MetricasNegocio.class);
        estrategias = mock(EstrategiaAdminService.class);
        ordenes = mock(RepositorioOrden.class);
        transacciones = mock(RepositorioTransaccionMercado.class);
        tenencias = mock(RepositorioTenenciaToken.class);
        usuarios = mock(RepositorioUsuario.class);
        cotizaciones = mock(RepositorioCotizacion.class);
        registro = new SimpleMeterRegistry();
        healthEndpoint = mock(HealthEndpoint.class);
        ObjectProvider<HealthEndpoint> proveedorSalud = mock(ObjectProvider.class);
        when(proveedorSalud.getIfAvailable()).thenReturn(healthEndpoint);
        when(metricas.consultarEnVivo()).thenReturn(new MetricasNegocio.MetricasEnVivo(
                OffsetDateTime.now(RELOJ), 3, 1, 0, 1500L, 1500L, 20, null));
        when(estrategias.consultar()).thenReturn(new EstrategiaAdminService.EstadoEstrategiaCotizacion(
                "PERFORMANCE_GENERAL", "Performance general", "desc", List.of(), "BASE_DATOS", null));
        when(ordenes.contarOrdenesCreadasPorDia(any(), any())).thenReturn(List.of());
        when(transacciones.resumirTransaccionesPorDia(any(), any())).thenReturn(List.of());
        when(cotizaciones.resumirPorDia(any(), any())).thenReturn(List.of());
        servicio = new ObservabilidadServiceImpl(metricas, estrategias,
                new RepositoriosObservabilidad(ordenes, transacciones, tenencias, usuarios, cotizaciones),
                registro, proveedorSalud, RELOJ);
    }

    // --- Negocio ---------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(ints = {7, 30, 90})
    void testSeriesTienenUnElementoPorDiaHastaHoyEnZonaArgentina(int dias) {
        var resumen = servicio.resumenNegocio(dias);

        assertThat(resumen.hasta()).isEqualTo(HOY);
        assertThat(resumen.desde()).isEqualTo(HOY.minusDays(dias - 1L));
        assertThat(resumen.serieMercado()).hasSize(dias);
        assertThat(resumen.serieCotizacion()).hasSize(dias);
        assertThat(resumen.serieMercado().getFirst().dia()).isEqualTo(resumen.desde());
        assertThat(resumen.serieMercado().getLast().dia()).isEqualTo(HOY);
        assertThat(resumen.serieMercado()).extracting(ActividadDiariaMercado::dia).isSorted();
    }

    @Test
    void testEnVivoDelegaEnElPuertoDeMetricas() {
        var resumen = servicio.resumenNegocio(7);

        assertThat(resumen.enVivo().rechazosSaldoInsuficiente()).isEqualTo(3);
        assertThat(resumen.enVivo().jugadoresCotizadosUltimoRecalculo()).isEqualTo(20);
    }

    @Test
    void testActualInformaLaEstrategiaActivaYLosTokensSinCustodio() {
        when(usuarios.idCustodio()).thenReturn(1L);
        when(tenencias.sumarTokensExcluyendoUsuario(1L)).thenReturn(320L);

        var resumen = servicio.resumenNegocio(7);

        assertThat(resumen.actual().estrategiaActiva().codigo()).isEqualTo("PERFORMANCE_GENERAL");
        assertThat(resumen.actual().estrategiaActiva().nombre()).isEqualTo("Performance general");
        assertThat(resumen.actual().tokensEnCirculacion()).isEqualTo(320);
    }

    @Test
    void testSerieDeMercadoCombinaOrdenesYTransaccionesPorDia() {
        LocalDate ayer = HOY.minusDays(1);
        when(ordenes.contarOrdenesCreadasPorDia(HOY.minusDays(6), HOY)).thenReturn(List.of(
                new ActividadDiariaMercado(ayer, 4, 2, 0, BigDecimal.ZERO)));
        when(transacciones.resumirTransaccionesPorDia(HOY.minusDays(6), HOY)).thenReturn(List.of(
                new ActividadDiariaMercado(ayer, 0, 0, 3, new BigDecimal("4200.50")),
                new ActividadDiariaMercado(HOY, 0, 0, 1, new BigDecimal("99.50"))));

        var serie = servicio.resumenNegocio(7).serieMercado();

        assertThat(serie.get(5)).isEqualTo(new ActividadDiariaMercado(ayer, 4, 2, 3, new BigDecimal("4200.50")));
        assertThat(serie.get(6).emparejamientos()).isEqualTo(1);
        assertThat(serie.get(6).ordenesCompra()).isZero();
    }

    @Test
    void testDiasSinActividadDeMercadoQuedanEnCero() {
        var serie = servicio.resumenNegocio(7).serieMercado();

        assertThat(serie).allSatisfy(dia -> {
            assertThat(dia.ordenesCompra()).isZero();
            assertThat(dia.emparejamientos()).isZero();
            assertThat(dia.volumenCreditos()).isEqualByComparingTo("0");
        });
    }

    @Test
    void testTotalesDelPeriodoSumanLaSerieConVolumenEnEscalaDos() {
        when(ordenes.contarOrdenesCreadasPorDia(any(), any())).thenReturn(List.of(
                new ActividadDiariaMercado(HOY, 3, 1, 0, BigDecimal.ZERO),
                new ActividadDiariaMercado(HOY.minusDays(2), 2, 4, 0, BigDecimal.ZERO)));
        when(transacciones.resumirTransaccionesPorDia(any(), any())).thenReturn(List.of(
                new ActividadDiariaMercado(HOY, 0, 0, 2, new BigDecimal("10.10")),
                new ActividadDiariaMercado(HOY.minusDays(2), 0, 0, 1, new BigDecimal("5.205"))));

        var totales = servicio.resumenNegocio(7).periodo();

        assertThat(totales.ordenesCompra()).isEqualTo(5);
        assertThat(totales.ordenesVenta()).isEqualTo(5);
        assertThat(totales.emparejamientos()).isEqualTo(3);
        assertThat(totales.volumenCreditos()).isEqualTo(new BigDecimal("15.31"));
    }

    @Test
    void testSerieDeCotizacionCompletaDiasVaciosConCeroYVariacionNula() {
        when(cotizaciones.resumirPorDia(HOY.minusDays(6), HOY)).thenReturn(List.of(
                new ActividadDiariaCotizacion(HOY.minusDays(1), 2400, new BigDecimal("0.018300"), 2300)));

        var serie = servicio.resumenNegocio(7).serieCotizacion();

        assertThat(serie).hasSize(7);
        assertThat(serie.get(5).jugadoresCotizados()).isEqualTo(2400);
        assertThat(serie.get(6).jugadoresCotizados()).isZero();
        assertThat(serie.get(6).variacionPromedio()).isNull();
    }

    @Test
    void testTotalesDeCotizacionPonderanLaVariacionPorCotizacionesConPrevio() {
        when(cotizaciones.resumirPorDia(any(), any())).thenReturn(List.of(
                new ActividadDiariaCotizacion(HOY.minusDays(3), 100, new BigDecimal("0.10"), 100),
                new ActividadDiariaCotizacion(HOY, 400, new BigDecimal("0.02"), 300)));

        var totales = servicio.resumenNegocio(7).periodo();

        assertThat(totales.cotizacionesCalculadas()).isEqualTo(500);
        // (0.10 * 100 + 0.02 * 300) / 400 = 0.04
        assertThat(totales.variacionPromedio()).isEqualByComparingTo("0.04");
    }

    @Test
    void testSinCotizacionesEnElPeriodoLaVariacionEsNula() {
        var totales = servicio.resumenNegocio(30).periodo();

        assertThat(totales.cotizacionesCalculadas()).isZero();
        assertThat(totales.variacionPromedio()).isNull();
    }

    // --- Tecnico ---------------------------------------------------------------------------

    @Test
    void testSaludDevuelveEstadoGlobalYComponentesSinDetalles() {
        HealthDescriptor db = mock(IndicatedHealthDescriptor.class);
        when(db.getStatus()).thenReturn(Status.DOWN);
        HealthDescriptor disco = mock(IndicatedHealthDescriptor.class);
        when(disco.getStatus()).thenReturn(Status.UP);
        CompositeHealthDescriptor sistema = mock(CompositeHealthDescriptor.class);
        when(sistema.getStatus()).thenReturn(Status.DOWN);
        when(sistema.getComponents()).thenReturn(Map.of("diskSpace", disco, "db", db));
        when(healthEndpoint.health()).thenReturn(sistema);

        var saludResumen = servicio.resumenTecnico().salud();

        assertThat(saludResumen.estado()).isEqualTo("DOWN");
        assertThat(saludResumen.componentes()).containsExactly(
                new ObservabilidadService.ComponenteSalud("db", "DOWN"),
                new ObservabilidadService.ComponenteSalud("diskSpace", "UP"));
    }

    @Test
    void testRequestsAgrupaPorEndpointExcluyeMonitoreoYCalculaTasas() {
        registrarRequests("GET", "/players/{id}", "SUCCESS", 8, 10);
        registrarRequests("GET", "/players/{id}", "CLIENT_ERROR", 2, 5);
        registrarRequests("POST", "/orders/buy", "SERVER_ERROR", 1, 100);
        registrarRequests("GET", "/actuator/prometheus", "SUCCESS", 50, 1);
        registrarRequests("GET", "/api/admin/observabilidad/tecnico", "SUCCESS", 40, 1);

        var requests = servicio.resumenTecnico().requests();

        assertThat(requests.total()).isEqualTo(11);
        assertThat(requests.tasaErrorCliente()).isEqualTo(2.0 / 11);
        assertThat(requests.tasaErrorServidor()).isEqualTo(1.0 / 11);
        assertThat(requests.latenciaMediaMs()).isEqualTo((8 * 10 + 2 * 5 + 100) / 11.0);
        assertThat(requests.endpoints()).extracting(ObservabilidadService.EndpointRequests::uri)
                .containsExactly("/players/{id}", "/orders/buy");
        var jugadores = requests.endpoints().getFirst();
        assertThat(jugadores.cantidad()).isEqualTo(10);
        assertThat(jugadores.erroresCliente()).isEqualTo(2);
        assertThat(jugadores.latenciaMediaMs()).isEqualTo(9.0);
    }

    @Test
    void testRequestsLimitaElTopADiezEndpointsOrdenadosPorCantidad() {
        for (int i = 1; i <= 12; i++) {
            registrarRequests("GET", "/recurso/" + i, "SUCCESS", i, 1);
        }

        var endpoints = servicio.resumenTecnico().requests().endpoints();

        assertThat(endpoints).hasSize(10);
        assertThat(endpoints.getFirst().uri()).isEqualTo("/recurso/12");
        assertThat(endpoints.getLast().uri()).isEqualTo("/recurso/3");
    }

    @Test
    void testSinRequestsLasTasasYLatenciaSonNulas() {
        var requests = servicio.resumenTecnico().requests();

        assertThat(requests.total()).isZero();
        assertThat(requests.tasaErrorCliente()).isNull();
        assertThat(requests.latenciaMediaMs()).isNull();
        assertThat(requests.endpoints()).isEmpty();
    }

    @Test
    void testJvmYPoolSumanGaugesPresentes() {
        Gauge.builder("jvm.memory.used", () -> 100).tag("area", "heap").tag("id", "eden").register(registro);
        Gauge.builder("jvm.memory.used", () -> 50).tag("area", "heap").tag("id", "old").register(registro);
        Gauge.builder("jvm.memory.used", () -> 999).tag("area", "nonheap").register(registro);
        Gauge.builder("jvm.memory.max", () -> -1).tag("area", "heap").tag("id", "eden").register(registro);
        Gauge.builder("jvm.memory.max", () -> 4000).tag("area", "heap").tag("id", "old").register(registro);
        Gauge.builder("jvm.threads.live", () -> 38).register(registro);
        Gauge.builder("process.cpu.usage", () -> 0.25).register(registro);
        TimeGauge.builder("process.uptime", () -> 120_000, TimeUnit.MILLISECONDS).register(registro);
        Gauge.builder("hikaricp.connections.active", () -> 2).tag("pool", "HikariPool-1").register(registro);
        Gauge.builder("hikaricp.connections.idle", () -> 8).tag("pool", "HikariPool-1").register(registro);

        var tecnico = servicio.resumenTecnico();

        assertThat(tecnico.jvm().memoriaHeapUsadaBytes()).isEqualTo(150);
        assertThat(tecnico.jvm().memoriaHeapMaximaBytes()).isEqualTo(4000);
        assertThat(tecnico.jvm().threadsVivos()).isEqualTo(38);
        assertThat(tecnico.jvm().cpuProceso()).isEqualTo(0.25);
        assertThat(tecnico.jvm().uptimeSegundos()).isEqualTo(120);
        assertThat(tecnico.poolConexiones().activas()).isEqualTo(2);
        assertThat(tecnico.poolConexiones().inactivas()).isEqualTo(8);
    }

    @Test
    void testMetricasAusentesDevuelvenNuloYSaludSinDescriptorEsDesconocida() {
        var tecnico = servicio.resumenTecnico();

        assertThat(tecnico.salud().estado()).isEqualTo("UNKNOWN");
        assertThat(tecnico.salud().componentes()).isEmpty();
        assertThat(tecnico.jvm().cpuSistema()).isNull();
        assertThat(tecnico.jvm().uptimeSegundos()).isNull();
        assertThat(tecnico.poolConexiones().pendientes()).isNull();
        assertThat(tecnico.instante()).isEqualTo(OffsetDateTime.now(RELOJ));
    }

    private void registrarRequests(String metodo, String uri, String outcome, int cantidad, long milisegundos) {
        Timer timer = Timer.builder(HTTP).tag("method", metodo).tag("uri", uri).tag("outcome", outcome)
                .register(registro);
        for (int i = 0; i < cantidad; i++) {
            timer.record(Duration.ofMillis(milisegundos));
        }
    }
}
