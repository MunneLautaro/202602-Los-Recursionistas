package unq.losrecursionistas.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "auditorias_transaccionales", indexes = {
        @Index(name = "idx_auditoria_entidad_fecha", columnList = "entidad_tipo,entidad_id,ejecutada_en"),
        @Index(name = "idx_auditoria_autor_fecha", columnList = "autor_id,ejecutada_en")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AuditoriaTransaccional {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entidad_tipo", nullable = false, updatable = false)
    private String entidadTipo;

    @Column(name = "entidad_id", nullable = false, updatable = false)
    private Long entidadId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false, updatable = false)
    private Usuario autor;

    @Column(name = "ejecutada_en", nullable = false, updatable = false)
    private OffsetDateTime ejecutadaEn;

    @Column(name = "detalle_cambio", nullable = false, updatable = false, length = 2000)
    private String detalleCambio;

    @Column(name = "estado_anterior", nullable = false, updatable = false, columnDefinition = "TEXT")
    private String estadoAnterior;

    @Column(name = "estado_posterior", nullable = false, updatable = false, columnDefinition = "TEXT")
    private String estadoPosterior;

    public static AuditoriaTransaccional crear(String tipo, Long entidadId, Usuario autor,
                                               String detalle, String anterior, String posterior) {
        return AuditoriaTransaccional.builder()
                .entidadTipo(tipo)
                .entidadId(entidadId)
                .autor(autor)
                .ejecutadaEn(OffsetDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires")))
                .detalleCambio(detalle)
                .estadoAnterior(anterior)
                .estadoPosterior(posterior)
                .build();
    }
}
