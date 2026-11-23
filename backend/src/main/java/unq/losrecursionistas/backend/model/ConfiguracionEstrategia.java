package unq.losrecursionistas.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_estrategia")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConfiguracionEstrategia {
    @Id
    private Long id = 1L;

    @Column(name = "estrategia_activa", nullable = false, length = 64)
    private String estrategiaActiva;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    public ConfiguracionEstrategia(String estrategiaActiva, LocalDateTime actualizadoEn) {
        this.estrategiaActiva = estrategiaActiva;
        this.actualizadoEn = actualizadoEn;
    }

    public void actualizar(String estrategiaActiva, LocalDateTime actualizadoEn) {
        this.estrategiaActiva = estrategiaActiva;
        this.actualizadoEn = actualizadoEn;
    }
}
