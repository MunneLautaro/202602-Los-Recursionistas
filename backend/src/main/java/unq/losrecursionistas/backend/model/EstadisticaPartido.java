package unq.losrecursionistas.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "estadisticas_partido",
        uniqueConstraints = @UniqueConstraint(name = "uk_estadistica_jugador_partido",
                columnNames = {"jugador_id", "partido_whoscored_id"}),
        indexes = {
                @Index(name = "idx_estadistica_jugador_fecha", columnList = "jugador_id,fecha_partido"),
                @Index(name = "idx_estadistica_fecha", columnList = "fecha_partido"),
                @Index(name = "idx_estadistica_torneo", columnList = "torneo")
        })
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EstadisticaPartido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;

    @Column(name = "partido_whoscored_id", nullable = false)
    private Long partidoWhoscoredId;
    @Column(nullable = false)
    private LocalDate fechaPartido;
    private String torneo;
    private Long torneoWhoscoredId;
    private LocalDate temporadaInicio;
    private LocalDate temporadaFin;
    private Long equipoWhoscoredId;
    private String equipoNombre;
    private Long rivalWhoscoredId;
    private String rivalNombre;
    private String localVisitante;
    @Column(nullable = false)
    private Integer minutos;
    private BigDecimal rating;
    private Integer goles;
    private Integer asistencias;
    private Integer tiros;
    private Integer tirosAlArco;
    private Integer pases;
    private Integer pasesClave;
    private Integer regates;
    private Integer tackles;
    private Integer intercepciones;
    private Integer faltasCometidas;
    private Integer amarillas;
    private Integer rojas;
    private BigDecimal precisionPases;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stats_crudas", columnDefinition = "jsonb")
    private Map<String, Object> statsCrudas;
}
