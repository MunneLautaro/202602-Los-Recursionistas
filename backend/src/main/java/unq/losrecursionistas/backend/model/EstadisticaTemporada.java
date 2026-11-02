package unq.losrecursionistas.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "estadisticas_temporada",
        uniqueConstraints = @UniqueConstraint(columnNames = {"jugador_id", "torneo_whoscored_id", "temporada_inicio"}))
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EstadisticaTemporada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;
    private String torneo;
    private Long torneoWhoscoredId;
    private LocalDate temporadaInicio;
    private LocalDate temporadaFin;
    private LocalDateTime ultimaActualizacion;
    private Integer apps;
    private Integer minutosTotales;
    private Integer goles;
    private Integer asistencias;
    private Integer tirosAlArco;
    private Integer pasesClave;
    private Integer regates;
    private Integer tackles;
    private Integer intercepciones;
    private Integer amarillas;
    private Integer rojas;
    private BigDecimal ratingPromedio;
    private BigDecimal precisionPasesPromedio;
}
