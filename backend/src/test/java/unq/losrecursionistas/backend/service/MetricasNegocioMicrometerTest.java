package unq.losrecursionistas.backend.service;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import unq.losrecursionistas.backend.model.TipoOrden;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioTenenciaToken;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioUsuario;
import unq.losrecursionistas.backend.service.impl.MetricasNegocioMicrometer;
import unq.losrecursionistas.backend.service.interfaces.MetricasNegocio.ResultadoRecalculo;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MetricasNegocioMicrometerTest {

    private static final String ORDENES_CREADAS = "mercado.ordenes.creadas";
    private static final String EMPAREJAMIENTOS = "mercado.emparejamientos";
    private static final String VOLUMEN = "mercado.volumen.creditos";
    private static final String RECALCULOS = "cotizacion.recalculos";
    private static final String RESULTADO = "resultado";

    private SimpleMeterRegistry registro;
    private RepositorioTenenciaToken tenencias;
    private RepositorioUsuario usuarios;
    private MetricasNegocioMicrometer metricas;

    @BeforeEach
    void configurar() {
        registro = new SimpleMeterRegistry();
        tenencias = mock(RepositorioTenenciaToken.class);
        usuarios = mock(RepositorioUsuario.class);
        metricas = new MetricasNegocioMicrometer(registro, tenencias, usuarios);
    }

    @Test
    void testTokensEnCirculacionExcluyeLaCuentaCustodioSinBloquearla() {
        when(usuarios.idCustodio()).thenReturn(1L);
        when(tenencias.sumarTokensExcluyendoUsuario(1L)).thenReturn(1500L);

        assertThat(registro.get("mercado.tokens.circulacion").gauge().value()).isEqualTo(1500);
        verify(usuarios, never()).buscarCustodio();
    }

    @Test
    void testTokensEnCirculacionSinCustodioSumaTodasLasTenencias() {
        when(usuarios.idCustodio()).thenReturn(null);
        when(tenencias.sumarTokensExcluyendoUsuario(-1L)).thenReturn(40L);

        assertThat(registro.get("mercado.tokens.circulacion").gauge().value()).isEqualTo(40);
    }

    @Test
    void testTokensEnCirculacionDevuelveCeroSiLaConsultaFalla() {
        when(usuarios.idCustodio()).thenThrow(new IllegalStateException("base no disponible"));

        assertThat(registro.get("mercado.tokens.circulacion").gauge().value()).isZero();
    }

    @AfterEach
    void limpiarSincronizacion() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void testOrdenCreadaSinTransaccionCuentaPorTipo() {
        metricas.registrarOrdenCreada(TipoOrden.COMPRA);
        metricas.registrarOrdenCreada(TipoOrden.COMPRA);
        metricas.registrarOrdenCreada(TipoOrden.VENTA);

        assertThat(contador(ORDENES_CREADAS, "tipo", "compra")).isEqualTo(2);
        assertThat(contador(ORDENES_CREADAS, "tipo", "venta")).isEqualTo(1);
    }

    @Test
    void testOrdenYEmparejamientoEnTransaccionCuentanRecienAlConfirmar() {
        TransactionSynchronizationManager.initSynchronization();

        metricas.registrarOrdenCreada(TipoOrden.COMPRA);
        metricas.registrarEmparejamiento(new BigDecimal("150.50"));

        assertThat(contador(ORDENES_CREADAS, "tipo", "compra")).isZero();
        assertThat(registro.get(EMPAREJAMIENTOS).counter().count()).isZero();

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);

        assertThat(contador(ORDENES_CREADAS, "tipo", "compra")).isEqualTo(1);
        assertThat(registro.get(EMPAREJAMIENTOS).counter().count()).isEqualTo(1);
        assertThat(registro.get(VOLUMEN).counter().count()).isEqualTo(150.50);
    }

    @Test
    void testOrdenEnTransaccionConRollbackNoCuenta() {
        TransactionSynchronizationManager.initSynchronization();

        metricas.registrarOrdenCreada(TipoOrden.VENTA);
        metricas.registrarEmparejamiento(BigDecimal.TEN);
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        assertThat(contador(ORDENES_CREADAS, "tipo", "venta")).isZero();
        assertThat(registro.get(EMPAREJAMIENTOS).counter().count()).isZero();
        assertThat(registro.get(VOLUMEN).counter().count()).isZero();
    }

    @Test
    void testEmparejamientoSinTransaccionSumaVolumen() {
        metricas.registrarEmparejamiento(new BigDecimal("100.25"));
        metricas.registrarEmparejamiento(new BigDecimal("49.75"));

        assertThat(registro.get(EMPAREJAMIENTOS).counter().count()).isEqualTo(2);
        assertThat(registro.get(VOLUMEN).counter().count()).isEqualTo(150.0);
    }

    @Test
    void testRechazoPorSaldoInsuficienteSeCuentaDeInmediato() {
        TransactionSynchronizationManager.initSynchronization();

        metricas.registrarRechazoSaldoInsuficiente();

        assertThat(registro.get("mercado.ordenes.rechazadas").tag("motivo", "saldo_insuficiente")
                .counter().count()).isEqualTo(1);
        assertThat(metricas.consultarEnVivo().rechazosSaldoInsuficiente()).isEqualTo(1);
    }

    @Test
    void testConsultarEnVivoSinRecalculosDevuelveNulos() {
        var enVivo = metricas.consultarEnVivo();

        assertThat(enVivo.inicio()).isNotNull();
        assertThat(enVivo.recalculosExitosos()).isZero();
        assertThat(enVivo.duracionUltimoRecalculoMs()).isNull();
        assertThat(enVivo.duracionPromedioRecalculoMs()).isNull();
        assertThat(enVivo.jugadoresCotizadosUltimoRecalculo()).isNull();
        assertThat(enVivo.variacionPromedioUltimoRecalculo()).isNull();
        assertThat(registro.get("cotizacion.variacion.promedio").gauge().value()).isNaN();
    }

    @Test
    void testRecalculoExitosoActualizaContadorDuracionYGauges() {
        metricas.registrarRecalculo(new ResultadoRecalculo(true, Duration.ofMillis(1000), 10, new BigDecimal("0.02")));
        metricas.registrarRecalculo(new ResultadoRecalculo(true, Duration.ofMillis(3000), 12, new BigDecimal("0.05")));

        var enVivo = metricas.consultarEnVivo();

        assertThat(contador(RECALCULOS, RESULTADO, "exitoso")).isEqualTo(2);
        assertThat(enVivo.recalculosExitosos()).isEqualTo(2);
        assertThat(enVivo.duracionUltimoRecalculoMs()).isEqualTo(3000);
        assertThat(enVivo.duracionPromedioRecalculoMs()).isEqualTo(2000);
        assertThat(enVivo.jugadoresCotizadosUltimoRecalculo()).isEqualTo(12);
        assertThat(enVivo.variacionPromedioUltimoRecalculo()).isEqualByComparingTo("0.05");
        assertThat(registro.get("cotizacion.jugadores.cotizados").gauge().value()).isEqualTo(12);
    }

    @Test
    void testRecalculoFallidoNoAlteraValoresDelUltimoExitoso() {
        metricas.registrarRecalculo(new ResultadoRecalculo(true, Duration.ofMillis(1000), 8, new BigDecimal("0.01")));
        metricas.registrarRecalculo(new ResultadoRecalculo(false, Duration.ofMillis(50000), 0, null));

        var enVivo = metricas.consultarEnVivo();

        assertThat(contador(RECALCULOS, RESULTADO, "fallido")).isEqualTo(1);
        assertThat(enVivo.recalculosFallidos()).isEqualTo(1);
        assertThat(enVivo.duracionPromedioRecalculoMs()).isEqualTo(1000);
        assertThat(enVivo.jugadoresCotizadosUltimoRecalculo()).isEqualTo(8);
    }

    @Test
    void testDuracionDeRecalculoNoPublicaHistogramaNiPercentiles() {
        metricas.registrarRecalculo(new ResultadoRecalculo(true, Duration.ofMillis(500), 1, null));

        var snapshot = registro.get("cotizacion.recalculo.duracion").tag(RESULTADO, "exitoso").timer().takeSnapshot();

        assertThat(snapshot.percentileValues()).isEmpty();
        assertThat(snapshot.histogramCounts()).isEmpty();
    }

    @Test
    void testLasMetricasSoloUsanEtiquetasDeCatalogosFijos() {
        metricas.registrarOrdenCreada(TipoOrden.COMPRA);
        metricas.registrarRechazoSaldoInsuficiente();

        Set<String> etiquetas = registro.getMeters().stream()
                .map(Meter::getId)
                .flatMap(id -> id.getTags().stream())
                .map(Tag::getKey)
                .collect(Collectors.toSet());

        assertThat(etiquetas).isSubsetOf("tipo", "motivo", RESULTADO);
    }

    private double contador(String nombre, String etiqueta, String valor) {
        return registro.get(nombre).tag(etiqueta, valor).counter().count();
    }
}
