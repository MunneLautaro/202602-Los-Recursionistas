package unq.losrecursionistas.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "casos_vinculacion")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CasoVinculacion {
    public enum Motivo { NO_ENCONTRADO, AMBIGUO }
    public enum Estado { PENDIENTE, RESUELTO, DESCARTADO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "id_whoscored", nullable = false)
    private Long idWhoscored;
    @Column(nullable = false)
    private String nombre;
    private String slugUrl;
    private LocalDate fechaNacimiento;
    private String nacionalidad;
    private Long equipoWhoscoredId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Motivo motivo;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "candidatos", columnDefinition = "jsonb")
    private Map<String, Object> candidatos;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ejecucion_id")
    private EjecucionIngesta ejecucion;
    @Column(nullable = false)
    private OffsetDateTime creadoEn;
    private OffsetDateTime actualizadoEn;
    @Column(length = 2000)
    private String detalleOperativo;
}
