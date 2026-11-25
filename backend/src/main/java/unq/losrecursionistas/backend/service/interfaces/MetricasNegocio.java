package unq.losrecursionistas.backend.service.interfaces;

import unq.losrecursionistas.backend.model.TipoOrden;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Puerto para registrar metricas de negocio de mercado y cotizacion.
 * Los servicios dependen solo de este contrato; la implementacion concreta
 * decide el mecanismo de metricas.
 */
public interface MetricasNegocio {

    /** Implementacion sin efecto para los constructores que no reciben el puerto. */
    MetricasNegocio SIN_REGISTRO = new MetricasNegocio() {
        private final OffsetDateTime inicio = OffsetDateTime.now(ZoneOffset.UTC);

        @Override
        public void registrarOrdenCreada(TipoOrden tipo) {
            // Sin registro: se usa cuando no hay mecanismo de metricas disponible.
        }

        @Override
        public void registrarRechazoSaldoInsuficiente() {
            // Sin registro: se usa cuando no hay mecanismo de metricas disponible.
        }

        @Override
        public void registrarEmparejamiento(BigDecimal total) {
            // Sin registro: se usa cuando no hay mecanismo de metricas disponible.
        }

        @Override
        public void registrarRecalculo(ResultadoRecalculo resultado) {
            // Sin registro: se usa cuando no hay mecanismo de metricas disponible.
        }

        @Override
        public MetricasEnVivo consultarEnVivo() {
            return new MetricasEnVivo(inicio, 0, 0, 0, null, null, null, null);
        }
    };

    /** Cuenta una orden creada por un usuario; se confirma al finalizar la transaccion. */
    void registrarOrdenCreada(TipoOrden tipo);

    /** Cuenta una orden de compra rechazada por saldo disponible insuficiente. */
    void registrarRechazoSaldoInsuficiente();

    /** Cuenta un emparejamiento y suma su total al volumen; se confirma al finalizar la transaccion. */
    void registrarEmparejamiento(BigDecimal total);

    /** Registra el resultado de un recalculo de cotizaciones. */
    void registrarRecalculo(ResultadoRecalculo resultado);

    /** Devuelve los valores acumulados desde el inicio de la aplicacion. */
    MetricasEnVivo consultarEnVivo();

    record ResultadoRecalculo(boolean exitoso, Duration duracion, int jugadoresCotizados,
                              BigDecimal variacionPromedio) {
    }

    record MetricasEnVivo(OffsetDateTime inicio, long rechazosSaldoInsuficiente,
                          long recalculosExitosos, long recalculosFallidos,
                          Long duracionUltimoRecalculoMs, Long duracionPromedioRecalculoMs,
                          Integer jugadoresCotizadosUltimoRecalculo,
                          BigDecimal variacionPromedioUltimoRecalculo) {
    }
}
