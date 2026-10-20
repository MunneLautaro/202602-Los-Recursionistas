package unq.losrecursionistas.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "ejecuciones_ingesta")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EjecucionIngesta {
    public enum Estado { ACTIVA, FINALIZADA, FALLIDA, RECHAZADA }
    public enum Alcance { COMPLETA, LIGA, EQUIPO, JUGADOR }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Alcance alcance;
    private Long ligaId;
    private Long equipoId;
    private Long jugadorId;
    private LocalDate temporadaInicio;
    private LocalDate temporadaFin;
    @Column(nullable = false)
    private OffsetDateTime inicio;
    private OffsetDateTime fin;
    private String motivoRechazo;
    private int jugadoresProcesados;
    private int jugadoresVinculados;
    private int jugadoresNoVinculados;
    private int partidosInsertados;
    private int partidosActualizados;
    private int errores;
    private int anomalias;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detalle", columnDefinition = "jsonb")
    private Map<String, Object> detalle;

    @Transient
    public Long getIdAlcance() {
        return switch (alcance) {
            case LIGA -> ligaId;
            case EQUIPO -> equipoId;
            case JUGADOR -> jugadorId;
            case COMPLETA -> null;
        };
    }

    public void setIdAlcance(Long idAlcance) {
        ligaId = null;
        equipoId = null;
        jugadorId = null;
        if (alcance == null || alcance == Alcance.COMPLETA) {
            return;
        }
        switch (alcance) {
            case LIGA -> ligaId = idAlcance;
            case EQUIPO -> equipoId = idAlcance;
            case JUGADOR -> jugadorId = idAlcance;
            case COMPLETA -> { }
        }
    }
}
