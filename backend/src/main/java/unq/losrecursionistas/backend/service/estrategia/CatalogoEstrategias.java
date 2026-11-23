package unq.losrecursionistas.backend.service.estrategia;

import java.util.List;
import java.util.Map;

public final class CatalogoEstrategias {
    public static final String GENERAL = "PERFORMANCE_GENERAL";
    public static final String POR_POSICION = "PERFORMANCE_POR_POSICION";

    private static final Map<String, Disponible> DISPONIBLES = Map.of(
            GENERAL, new Disponible(GENERAL, "Rendimiento general",
                    "Calcula segun pesos generales para todas las metricas."),
            POR_POSICION, new Disponible(POR_POSICION, "Rendimiento por posicion",
                    "Calcula segun pesos especificos de la posicion; usa los pesos generales si la posicion es desconocida.")
    );

    private CatalogoEstrategias() {
    }

    public static List<Disponible> todas() {
        return List.of(DISPONIBLES.get(GENERAL), DISPONIBLES.get(POR_POSICION));
    }

    public static Disponible obtener(String codigo) {
        return DISPONIBLES.get(codigo);
    }

    public record Disponible(String codigo, String nombre, String descripcion) {
    }
}
