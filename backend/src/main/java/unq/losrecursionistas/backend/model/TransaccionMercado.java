package unq.losrecursionistas.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "transacciones_mercado", indexes = {
        @Index(name = "idx_transaccion_comprador_fecha", columnList = "comprador_id,ejecutada_en"),
        @Index(name = "idx_transaccion_vendedor_fecha", columnList = "vendedor_id,ejecutada_en")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TransaccionMercado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_compra_id", nullable = false, updatable = false)
    private Orden ordenCompra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_venta_id", nullable = false, updatable = false)
    private Orden ordenVenta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jugador_id", nullable = false, updatable = false)
    private Jugador jugador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprador_id", nullable = false, updatable = false)
    private Usuario comprador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendedor_id", nullable = false, updatable = false)
    private Usuario vendedor;

    @Column(nullable = false, updatable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 19, scale = 2,
            updatable = false)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal total;

    @Column(name = "ejecutada_en", nullable = false, updatable = false)
    private OffsetDateTime ejecutadaEn;

    public static TransaccionMercado crear(Orden compra, Orden venta, int cantidad) {
        BigDecimal total = compra.getPrecioUnitario().multiply(BigDecimal.valueOf(cantidad));
        return TransaccionMercado.builder()
                .ordenCompra(compra)
                .ordenVenta(venta)
                .jugador(compra.getJugador())
                .comprador(compra.getUsuario())
                .vendedor(venta.getUsuario())
                .cantidad(cantidad)
                .precioUnitario(compra.getPrecioUnitario())
                .total(total)
                .ejecutadaEn(OffsetDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires")))
                .build();
    }
}
