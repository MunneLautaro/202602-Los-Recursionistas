package unq.losrecursionistas.backend.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioJugador;
import unq.losrecursionistas.backend.service.interfaces.CotizacionService;
import unq.losrecursionistas.backend.service.interfaces.LiquidezInicialService;

@Component
@ConditionalOnProperty(name = "scheduler.cotizacion.enabled", havingValue = "true", matchIfMissing = true)
public class CotizacionScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CotizacionScheduler.class);
    private final CotizacionService servicio;
    private final RepositorioJugador jugadores;
    private final LiquidezInicialService liquidezInicial;

    public CotizacionScheduler(CotizacionService servicio,
                               RepositorioJugador jugadores,
                               LiquidezInicialService liquidezInicial) {
        this.servicio = servicio;
        this.jugadores = jugadores;
        this.liquidezInicial = liquidezInicial;
    }

    @Scheduled(cron = "${scheduler.cotizacion.cron}", zone = "${scheduler.cotizacion.zone:America/Argentina/Buenos_Aires}")
    public void ejecutarCotizacion() {
        servicio.recalcular();
        int procesados = 0;
        int errores = 0;
        for (var jugador : jugadores.obtenerJugadoresActivos()) {
            try {
                liquidezInicial.inicializar(jugador);
                procesados++;
            } catch (RuntimeException exception) {
                LOGGER.warn("No se pudo inicializar liquidez para el jugador '{}' (ID: {}): {}",
                        jugador.getNombre(), jugador.getId(), exception.getMessage());
                errores++;
            }
        }
        LOGGER.info("Proceso de liquidez semanal finalizado. Jugadores procesados: {}, Errores: {}.",
                procesados, errores);
    }
}
