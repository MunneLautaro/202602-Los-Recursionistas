package unq.losrecursionistas.backend.service.interfaces;

import java.time.LocalDate;

/**
 * Ejecuta en segundo plano el recálculo de cotizaciones para la ventana indicada.
 * Al terminar (con éxito o error) libera la reserva tomada con
 * {@link CotizacionService#reservarRecalculo()}.
 */
public interface RecalculoCotizacionAsincrono {
    void ejecutar(LocalDate inicio, LocalDate fin);
}
