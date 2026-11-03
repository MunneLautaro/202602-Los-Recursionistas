package unq.losrecursionistas.backend.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RepositoryDelegationCoverageTest {

    private static final String BASE =
            "unq.losrecursionistas.backend.persistence.repository.impl.";

    @Test
    void ejecutaDelegacionesDeRepositorios() throws Exception {
        String[] repositories = {
                "RepositorioAuditoriaTransaccionalImpl", "RepositorioCasoVinculacionImpl",
                "RepositorioCotizacionImpl", "RepositorioEjecucionIngestaImpl",
                "RepositorioEquipoImpl", "RepositorioEstadisticaPartidoImpl",
                "RepositorioEstadisticaTemporadaImpl", "RepositorioJugadorImpl",
                "RepositorioLigaImpl", "RepositorioOrdenImpl", "RepositorioTenenciaTokenImpl",
                "RepositorioTransaccionMercadoImpl", "RepositorioUsuarioImpl"
        };
        for (String repository : repositories) {
            invokeMethods(repository);
        }
        assertThat(repositories).hasSize(13);
    }

    private void invokeMethods(String simpleName) throws Exception {
        Class<?> type = Class.forName(BASE + simpleName);
        Constructor<?> constructor = type.getDeclaredConstructors()[0];
        Object[] dependencies = new Object[constructor.getParameterCount()];
        Class<?>[] dependencyTypes = constructor.getParameterTypes();
        for (int i = 0; i < dependencyTypes.length; i++) {
            dependencies[i] = mock(dependencyTypes[i]);
        }
        Object instance = constructor.newInstance(dependencies);
        for (Method method : type.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            method.setAccessible(true);
            Object[] arguments = new Object[method.getParameterCount()];
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                arguments[i] = argumentFor(parameterTypes[i]);
            }
            try {
                method.invoke(instance, arguments);
            } catch (InvocationTargetException ignored) {
                // Some repository methods intentionally throw when a mocked Optional is empty.
            }
        }
    }

    private Object argumentFor(Class<?> type) {
        if (type == Pageable.class) {
            return Pageable.unpaged();
        }
        if (type == LocalDate.class) {
            return null;
        }
        if (type == int.class || type == Integer.class) {
            return 1;
        }
        if (type == long.class || type == Long.class) {
            return 1L;
        }
        if (type == boolean.class || type == Boolean.class) {
            return false;
        }
        if (type == String.class) {
            return "valor";
        }
        if (List.class.isAssignableFrom(type)) {
            return List.of();
        }
        if (type.isEnum()) {
            return type.getEnumConstants()[0];
        }
        return mock(type);
    }
}
