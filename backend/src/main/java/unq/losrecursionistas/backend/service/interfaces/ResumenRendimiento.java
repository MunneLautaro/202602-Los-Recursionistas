package unq.losrecursionistas.backend.service.interfaces;

import java.math.BigDecimal;

public record ResumenRendimiento(
        int goles, int asistencias, int tiros, int pasesClave, int regates,
        int tackles, int intercepciones, int minutos, BigDecimal rating) {
    public ResumenRendimiento {
        rating = rating == null ? BigDecimal.ZERO : rating;
    }
}
