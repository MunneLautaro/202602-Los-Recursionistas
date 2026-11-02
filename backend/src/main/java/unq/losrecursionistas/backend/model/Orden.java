package unq.losrecursionistas.backend.model;

import jakarta.persistence.*;
import lombok.*;
import unq.losrecursionistas.backend.exceptions.ExcepcionCantidadInvalida;
import unq.losrecursionistas.backend.exceptions.ExcepcionDominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "ordenes", indexes = {
        @Index(name = "idx_ordenes_activas_fifo",
                columnList = "jugador_id,tipo,precio_unitario,fecha_creacion,id")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Orden {
    private static final ZoneId ZONA_ARGENTINA =
            ZoneId.of("America/Argentina/Buenos_Aires");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoOrden tipo;

    @Column(name = "cantidad_original", nullable = false, updatable = false)
    private Integer cantidadOriginal;

    @Column(name = "cantidad_disponible", nullable = false)
    private Integer cantidadDisponible;

    @Column(name = "precio_unitario", nullable = false, precision = 19, scale = 2,
            updatable = false)
    private BigDecimal precioUnitario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOrden estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "es_liquidez_inicial", nullable = false)
    private boolean esLiquidezInicial;

    public static Orden crear(Usuario usuario, Jugador jugador, TipoOrden tipo,
                              int cantidad, BigDecimal precioUnitario,
                              boolean esLiquidezInicial) {
        if (usuario == null || jugador == null || tipo == null
                || cantidad <= 0 || precioUnitario == null
                || precioUnitario.signum() <= 0) {
            throw new ExcepcionCantidadInvalida("Los datos de la orden son invalidos");
        }
        return Orden.builder()
                .usuario(usuario)
                .jugador(jugador)
                .tipo(tipo)
                .cantidadOriginal(cantidad)
                .cantidadDisponible(cantidad)
                .precioUnitario(precioUnitario.setScale(2, RoundingMode.HALF_UP))
                .estado(EstadoOrden.ACTIVA)
                .fechaCreacion(OffsetDateTime.now(ZONA_ARGENTINA))
                .esLiquidezInicial(esLiquidezInicial)
                .build();
    }

    public void ejecutar(int cantidad) {
        if (cantidad <= 0 || cantidad > cantidadDisponible) {
            throw new ExcepcionDominio("La ejecucion excede la cantidad disponible");
        }
        cantidadDisponible -= cantidad;
        if (cantidadDisponible == 0) {
            estado = EstadoOrden.COMPLETADA;
        }
    }

    public BigDecimal precioTotalRemanente() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidadDisponible))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
