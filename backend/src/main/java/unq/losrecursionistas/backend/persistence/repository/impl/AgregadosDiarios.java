package unq.losrecursionistas.backend.persistence.repository.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Utilidades de los repositorios para las consultas agregadas por dia: limite de rango
 * (las series estan acotadas por diseno, Principio IV) y conversion de valores nativos.
 */
final class AgregadosDiarios {
    static final int MAXIMO_DIAS = 90;

    private AgregadosDiarios() {
    }

    static void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || desde.isAfter(hasta)
                || ChronoUnit.DAYS.between(desde, hasta) + 1 > MAXIMO_DIAS) {
            throw new IllegalArgumentException("El rango de dias debe ser valido y de a lo sumo " + MAXIMO_DIAS + " dias");
        }
    }

    static LocalDate dia(Object valor) {
        if (valor instanceof LocalDate fecha) {
            return fecha;
        }
        if (valor instanceof java.sql.Date fecha) {
            return fecha.toLocalDate();
        }
        return LocalDate.parse(String.valueOf(valor));
    }

    static long entero(Number valor) {
        return valor == null ? 0 : valor.longValue();
    }

    static BigDecimal decimal(Number valor, int escala) {
        if (valor == null) {
            return null;
        }
        BigDecimal numero = valor instanceof BigDecimal exacto ? exacto : new BigDecimal(valor.toString());
        return numero.setScale(escala, RoundingMode.HALF_UP);
    }
}
