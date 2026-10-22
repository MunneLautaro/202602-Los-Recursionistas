package unq.losrecursionistas.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cotizaciones", indexes = {
        @Index(name = "idx_cotizacion_jugador_timestamp",
                columnList = "jugador_id,timestamp_calculo DESC"),
        @Index(name = "idx_cotizacion_timestamp",
                columnList = "timestamp_calculo DESC")
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cotizacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(name = "timestamp_calculo", nullable = false)
    private LocalDateTime timestampCalculo;

    @Column(nullable = false, updatable = false)
    private String estrategiaUsada;

    @Column(name = "ventana_inicio", nullable = false)
    private LocalDate ventanaInicio;

    @Column(name = "ventana_fin", nullable = false)
    private LocalDate ventanaFin;

    public static Cotizacion crear(Jugador jugador, BigDecimal valor, LocalDateTime timestamp,
                                   String estrategia, LocalDate inicio, LocalDate fin) {
        if (jugador == null || valor == null || estrategia == null || estrategia.isBlank()
                || timestamp == null || inicio == null || fin == null || inicio.isAfter(fin)) {
            throw new IllegalArgumentException("Los datos de cotizacion son invalidos");
        }
        return Cotizacion.builder()
                .jugador(jugador)
                .valor(valor.setScale(2, RoundingMode.HALF_UP))
                .timestampCalculo(timestamp)
                .estrategiaUsada(estrategia)
                .ventanaInicio(inicio)
                .ventanaFin(fin)
                .build();
    }
}
