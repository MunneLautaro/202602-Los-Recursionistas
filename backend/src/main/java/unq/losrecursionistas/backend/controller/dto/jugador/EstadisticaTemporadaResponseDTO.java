package unq.losrecursionistas.backend.controller.dto.jugador;

import unq.losrecursionistas.backend.model.EstadisticaTemporada;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record EstadisticaTemporadaResponseDTO(
        Long id,
        Long jugadorId,
        String jugadorNombre,
        String posicion,
        String equipoNombre,
        String torneo,
        Long torneoWhoscoredId,
        LocalDate temporadaInicio,
        LocalDate temporadaFin,
        LocalDateTime ultimaActualizacion,
        Integer apps,
        Integer minutosTotales,
        Integer goles,
        Integer asistencias,
        Integer tirosAlArco,
        Integer pasesClave,
        Integer regates,
        Integer tackles,
        Integer intercepciones,
        Integer amarillas,
        Integer rojas,
        BigDecimal ratingPromedio,
        BigDecimal precisionPasesPromedio
) {
    public static EstadisticaTemporadaResponseDTO desdeModelo(EstadisticaTemporada estadistica) {
        var jugador = estadistica.getJugador();

        return new EstadisticaTemporadaResponseDTO(
                estadistica.getId(),
                jugador.getId(),
                jugador.getNombre(),
                jugador.getPosicion(),
                jugador.getEquipo() == null ? null : jugador.getEquipo().getNombre(),
                estadistica.getTorneo(),
                estadistica.getTorneoWhoscoredId(),
                estadistica.getTemporadaInicio(),
                estadistica.getTemporadaFin(),
                estadistica.getUltimaActualizacion(),
                estadistica.getApps(),
                estadistica.getMinutosTotales(),
                estadistica.getGoles(),
                estadistica.getAsistencias(),
                estadistica.getTirosAlArco(),
                estadistica.getPasesClave(),
                estadistica.getRegates(),
                estadistica.getTackles(),
                estadistica.getIntercepciones(),
                estadistica.getAmarillas(),
                estadistica.getRojas(),
                estadistica.getRatingPromedio(),
                estadistica.getPrecisionPasesPromedio()
        );
    }
}
