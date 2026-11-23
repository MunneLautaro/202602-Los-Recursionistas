package unq.losrecursionistas.backend.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cambios_estrategia")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CambioEstrategia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "estrategia_anterior", length = 64, updatable = false)
    private String estrategiaAnterior;

    @Column(name = "estrategia_nueva", nullable = false, length = 64, updatable = false)
    private String estrategiaNueva;

    @Column(name = "usuario_admin", nullable = false, length = 120, updatable = false)
    private String usuarioAdmin;

    @Column(name = "timestamp_cambio", nullable = false, updatable = false)
    private LocalDateTime timestampCambio;

    public CambioEstrategia(String estrategiaAnterior, String estrategiaNueva,
                            String usuarioAdmin, LocalDateTime timestampCambio) {
        this.estrategiaAnterior = estrategiaAnterior;
        this.estrategiaNueva = estrategiaNueva;
        this.usuarioAdmin = usuarioAdmin;
        this.timestampCambio = timestampCambio;
    }
}
