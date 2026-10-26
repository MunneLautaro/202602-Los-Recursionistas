package unq.losrecursionistas.backend.model;

import jakarta.persistence.*;
import lombok.*;
import unq.losrecursionistas.backend.exceptions.ExcepcionCantidadInvalida;
import unq.losrecursionistas.backend.exceptions.ExcepcionRecursosInsuficientes;

@Entity
@Table(name = "tenencias_token",
        uniqueConstraints = @UniqueConstraint(name = "uk_tenencia_usuario_jugador",
                columnNames = {"usuario_id", "jugador_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TenenciaToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;

    @Column(name = "cantidad_total", nullable = false)
    @Builder.Default
    private Integer cantidadTotal = 0;

    @Column(name = "cantidad_reservada", nullable = false)
    @Builder.Default
    private Integer cantidadReservada = 0;

    public int cantidadDisponible() {
        return cantidadTotal - cantidadReservada;
    }

    public void acreditar(int cantidad) {
        validar(cantidad);
        cantidadTotal += cantidad;
    }

    public void reservar(int cantidad) {
        validar(cantidad);
        if (cantidad > cantidadDisponible()) {
            throw new ExcepcionRecursosInsuficientes("Tokens disponibles insuficientes");
        }
        cantidadReservada += cantidad;
    }

    public void liberar(int cantidad) {
        validar(cantidad);
        if (cantidad > cantidadReservada) {
            throw new ExcepcionRecursosInsuficientes("No hay tokens reservados suficientes");
        }
        cantidadReservada -= cantidad;
    }

    public void debitar(int cantidad) {
        validar(cantidad);
        if (cantidad > cantidadTotal - cantidadReservada) {
            throw new ExcepcionRecursosInsuficientes("Tokens disponibles insuficientes");
        }
        cantidadTotal -= cantidad;
    }

    private static void validar(int cantidad) {
        if (cantidad <= 0) {
            throw new ExcepcionCantidadInvalida("La cantidad debe ser positiva");
        }
    }
}
