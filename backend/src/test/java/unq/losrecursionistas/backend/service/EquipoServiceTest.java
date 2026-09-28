package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unq.losrecursionistas.backend.model.Equipo;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEquipo;
import unq.losrecursionistas.backend.service.impl.EquipoServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipoServiceTest {

    @Mock
    private RepositorioEquipo repositorioEquipo;

    @InjectMocks
    private EquipoServiceImpl equipoService;

    private Equipo equipoTest;

    @BeforeEach
    void setUp() {
        equipoTest = Equipo.builder()
                .id(1L)
                .idExterno(64L)
                .nombre("Liverpool FC")
                .sigla("LIV")
                .build();
    }

    @Test
    @DisplayName("Caso Feliz: guardar persiste y retorna el equipo correctamente")
    void guardarExitoso() {
        when(repositorioEquipo.guardar(equipoTest)).thenReturn(equipoTest);

        Equipo resultado = equipoService.guardar(equipoTest);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Liverpool FC");
        verify(repositorioEquipo).guardar(equipoTest);
    }

    @Test
    @DisplayName("Caso Feliz: buscarPorIdExterno retorna el equipo existente")
    void buscarPorIdExternoExitoso() {
        when(repositorioEquipo.buscarPorIdExterno(64L, equipoTest)).thenReturn(equipoTest);

        Equipo resultado = equipoService.buscarPorIdExterno(64L, equipoTest);

        assertThat(resultado).isEqualTo(equipoTest);
        verify(repositorioEquipo).buscarPorIdExterno(64L, equipoTest);
    }

    @Test
    @DisplayName("Caso Borde / No Feliz: buscarPorIdExterno con id nulo o no encontrado retorna fallback")
    void buscarPorIdExternoInexistenteRetornaFallback() {
        Equipo fallbackEquipo = Equipo.builder().nombre("Equipo Default").build();
        when(repositorioEquipo.buscarPorIdExterno(null, fallbackEquipo)).thenReturn(fallbackEquipo);

        Equipo resultado = equipoService.buscarPorIdExterno(null, fallbackEquipo);

        assertThat(resultado).isEqualTo(fallbackEquipo);
        verify(repositorioEquipo).buscarPorIdExterno(null, fallbackEquipo);
    }

    @Test
    @DisplayName("Caso Feliz: guardarOActualizar persiste cambios y devuelve el equipo actualizado")
    void guardarOActualizarExitoso() {
        when(repositorioEquipo.guardarOActualizar(equipoTest)).thenReturn(equipoTest);

        Equipo resultado = equipoService.guardarOActualizar(equipoTest);

        assertThat(resultado).isEqualTo(equipoTest);
        verify(repositorioEquipo).guardarOActualizar(equipoTest);
    }
}
