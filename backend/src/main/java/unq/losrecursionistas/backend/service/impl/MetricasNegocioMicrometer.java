package unq.losrecursionistas.backend.service.impl;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import unq.losrecursionistas.backend.model.TipoOrden;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioTenenciaToken;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioUsuario;
import unq.losrecursionistas.backend.service.interfaces.MetricasNegocio;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementacion del puerto {@link MetricasNegocio} con Micrometer. Es la unica
 * clase de negocio que conoce el registro de metricas.
 */
@Service
public class MetricasNegocioMicrometer implements MetricasNegocio {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricasNegocioMicrometer.class);

    static final String ORDENES_CREADAS = "mercado.ordenes.creadas";
    static final String EMPAREJAMIENTOS = "mercado.emparejamientos";
    static final String VOLUMEN_CREDITOS = "mercado.volumen.creditos";
    static final String ORDENES_RECHAZADAS = "mercado.ordenes.rechazadas";
    static final String TOKENS_CIRCULACION = "mercado.tokens.circulacion";
    static final String RECALCULOS = "cotizacion.recalculos";
    static final String DURACION_RECALCULO = "cotizacion.recalculo.duracion";
    static final String JUGADORES_COTIZADOS = "cotizacion.jugadores.cotizados";
    static final String VARIACION_PROMEDIO = "cotizacion.variacion.promedio";

    private static final String TAG_RESULTADO = "resultado";
    private static final String EXITOSO = "exitoso";
    private static final String FALLIDO = "fallido";
    private static final ZoneId ZONA_ARGENTINA = ZoneId.of("America/Argentina/Buenos_Aires");
    /** Id inexistente: sin custodio no se excluye ningun usuario. */
    private static final long SIN_CUSTODIO = -1L;

    private final Map<TipoOrden, Counter> ordenesCreadas = new EnumMap<>(TipoOrden.class);
    private final Counter emparejamientos;
    private final Counter volumenCreditos;
    private final Counter rechazosSaldo;
    private final Counter recalculosExitosos;
    private final Counter recalculosFallidos;
    private final Timer duracionExitosa;
    private final Timer duracionFallida;
    private final AtomicLong duracionUltimoExitosoMs = new AtomicLong(-1);
    private final AtomicReference<Integer> jugadoresCotizados = new AtomicReference<>();
    private final AtomicReference<BigDecimal> variacionPromedio = new AtomicReference<>();
    private final OffsetDateTime inicio = OffsetDateTime.now(ZONA_ARGENTINA);

    public MetricasNegocioMicrometer(MeterRegistry registro, RepositorioTenenciaToken tenencias,
                                     RepositorioUsuario usuarios) {
        Gauge.builder(TOKENS_CIRCULACION, () -> tokensEnCirculacion(tenencias, usuarios))
                .description("Tokens en poder de usuarios, sin la cuenta custodio").register(registro);
        for (TipoOrden tipo : TipoOrden.values()) {
            ordenesCreadas.put(tipo, Counter.builder(ORDENES_CREADAS)
                    .description("Ordenes creadas por usuarios")
                    .tag("tipo", tipo.name().toLowerCase(Locale.ROOT))
                    .register(registro));
        }
        emparejamientos = Counter.builder(EMPAREJAMIENTOS)
                .description("Transacciones de mercado ejecutadas").register(registro);
        volumenCreditos = Counter.builder(VOLUMEN_CREDITOS)
                .description("Creditos operados en transacciones de mercado").register(registro);
        rechazosSaldo = Counter.builder(ORDENES_RECHAZADAS)
                .description("Ordenes de compra rechazadas")
                .tag("motivo", "saldo_insuficiente").register(registro);
        recalculosExitosos = Counter.builder(RECALCULOS)
                .description("Recalculos de cotizacion ejecutados")
                .tag(TAG_RESULTADO, EXITOSO).register(registro);
        recalculosFallidos = Counter.builder(RECALCULOS)
                .description("Recalculos de cotizacion ejecutados")
                .tag(TAG_RESULTADO, FALLIDO).register(registro);
        duracionExitosa = Timer.builder(DURACION_RECALCULO)
                .description("Duracion de los recalculos de cotizacion")
                .tag(TAG_RESULTADO, EXITOSO).register(registro);
        duracionFallida = Timer.builder(DURACION_RECALCULO)
                .description("Duracion de los recalculos de cotizacion")
                .tag(TAG_RESULTADO, FALLIDO).register(registro);
        Gauge.builder(JUGADORES_COTIZADOS, jugadoresCotizados,
                        valor -> valor.get() == null ? Double.NaN : valor.get())
                .description("Jugadores cotizados en el ultimo recalculo exitoso").register(registro);
        Gauge.builder(VARIACION_PROMEDIO, variacionPromedio,
                        valor -> valor.get() == null ? Double.NaN : valor.get().doubleValue())
                .description("Variacion promedio del ultimo recalculo exitoso (fraccion)").register(registro);
    }

    @Override
    public void registrarOrdenCreada(TipoOrden tipo) {
        Counter contador = ordenesCreadas.get(tipo);
        if (contador != null) {
            alConfirmar(contador::increment);
        }
    }

    @Override
    public void registrarRechazoSaldoInsuficiente() {
        rechazosSaldo.increment();
    }

    @Override
    public void registrarEmparejamiento(BigDecimal total) {
        double monto = total == null ? 0 : total.doubleValue();
        alConfirmar(() -> {
            emparejamientos.increment();
            volumenCreditos.increment(monto);
        });
    }

    @Override
    public void registrarRecalculo(ResultadoRecalculo resultado) {
        if (resultado.exitoso()) {
            recalculosExitosos.increment();
            duracionExitosa.record(resultado.duracion());
            duracionUltimoExitosoMs.set(resultado.duracion().toMillis());
            jugadoresCotizados.set(resultado.jugadoresCotizados());
            variacionPromedio.set(resultado.variacionPromedio());
        } else {
            recalculosFallidos.increment();
            duracionFallida.record(resultado.duracion());
        }
    }

    @Override
    public MetricasEnVivo consultarEnVivo() {
        long exitosos = duracionExitosa.count();
        Long ultima = duracionUltimoExitosoMs.get() < 0 ? null : duracionUltimoExitosoMs.get();
        Long promedio = exitosos == 0 ? null
                : Math.round(duracionExitosa.totalTime(TimeUnit.MILLISECONDS) / exitosos);
        return new MetricasEnVivo(inicio, (long) rechazosSaldo.count(),
                (long) recalculosExitosos.count(), (long) recalculosFallidos.count(),
                ultima, promedio, jugadoresCotizados.get(), variacionPromedio.get());
    }

    /**
     * Tokens en poder de usuarios excluyendo la cuenta custodio. Usa una consulta sin bloqueo
     * (la busqueda bloqueante del custodio falla en transacciones de solo lectura). Si no hay
     * custodio suma todas las tenencias; si la consulta falla devuelve 0 para no interrumpir
     * la exposicion de metricas.
     */
    static double tokensEnCirculacion(RepositorioTenenciaToken tenencias, RepositorioUsuario usuarios) {
        try {
            Long custodio = usuarios.idCustodio();
            return tenencias.sumarTokensExcluyendoUsuario(custodio == null ? SIN_CUSTODIO : custodio);
        } catch (RuntimeException excepcion) {
            LOGGER.warn("No se pudieron calcular los tokens en circulacion: {}", excepcion.getMessage(), excepcion);
            return 0;
        }
    }

    /** Ejecuta la accion al confirmarse la transaccion activa, o de inmediato si no hay una. */
    private static void alConfirmar(Runnable accion) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    accion.run();
                }
            });
        } else {
            accion.run();
        }
    }
}
