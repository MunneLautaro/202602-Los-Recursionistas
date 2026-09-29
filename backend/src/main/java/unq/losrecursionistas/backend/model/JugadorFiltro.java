package unq.losrecursionistas.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class JugadorFiltro {
    private String nombre;
    private String posicion;
    private Long ligaId;
    private Long equipoId;
}
