package unq.losrecursionistas.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import unq.losrecursionistas.backend.model.Equipo;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.model.JugadorFiltro;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioJugador;
import unq.losrecursionistas.backend.service.impl.JugadorServiceImpl;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JugadorServiceTest {

    @Mock
    private RepositorioJugador repositorioJugador;

    @InjectMocks
    private JugadorServiceImpl jugadorService;

    private Jugador jugador1;

    @BeforeEach
    void setUp() {
        Equipo equipo = Equipo.builder().nombre("River Plate").build();
        jugador1 = Jugador.builder()
                .id(1L)
                .nombre("Franco Armani")
                .posicion("Arquero")
                .equipo(equipo)
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("Caso Feliz: guardarTodos invoca el repositorio con la lista de jugadores")
    void guardarTodosExitoso() {
        List<Jugador> jugadores = List.of(jugador1);

        jugadorService.guardarTodos(jugadores);

        verify(repositorioJugador).guardarTodos(jugadores);
    }

    @Test
    @DisplayName("Caso Borde: guardarTodos con lista vacía")
    void guardarTodosListaVacia() {
        List<Jugador> jugadoresVacios = Collections.emptyList();

        jugadorService.guardarTodos(jugadoresVacios);

        verify(repositorioJugador).guardarTodos(jugadoresVacios);
    }

    @Test
    @DisplayName("Caso Feliz: obtenerJugadorPorId retorna el jugador cuando existe")
    void obtenerJugadorPorIdExitoso() {
        when(repositorioJugador.obtenerJugadorPorId(1L)).thenReturn(jugador1);

        Jugador resultado = jugadorService.obtenerJugadorPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Franco Armani");
        verify(repositorioJugador).obtenerJugadorPorId(1L);
    }

    @Test
    @DisplayName("Caso No Feliz / Borde: obtenerJugadorPorId retorna null cuando el id no existe")
    void obtenerJugadorPorIdInexistenteRetornaNull() {
        when(repositorioJugador.obtenerJugadorPorId(999L)).thenReturn(null);

        Jugador resultado = jugadorService.obtenerJugadorPorId(999L);

        assertThat(resultado).isNull();
        verify(repositorioJugador).obtenerJugadorPorId(999L);
    }

    @Test
    @DisplayName("Caso Feliz: buscarJugadoresConFiltro aplica la paginación correcta de 12 elementos por página")
    void buscarJugadoresConFiltroExitoso() {
        JugadorFiltro filtro = JugadorFiltro.builder().nombre("Franco").build();
        Pageable expectedPageable = PageRequest.of(0, 12, Sort.by("nombre").ascending());
        Page<Jugador> pageMock = new PageImpl<>(List.of(jugador1), expectedPageable, 1);

        when(repositorioJugador.buscarJugadoresConFiltro(eq(filtro), eq(expectedPageable))).thenReturn(pageMock);

        Page<Jugador> resultado = jugadorService.buscarJugadoresConFiltro(filtro, 0);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).containsExactly(jugador1);
        verify(repositorioJugador).buscarJugadoresConFiltro(filtro, expectedPageable);
    }

    @Test
    @DisplayName("Caso Borde: buscarJugadoresConFiltro para una página posterior")
    void buscarJugadoresConFiltroPaginaPosterior() {
        JugadorFiltro filtro = JugadorFiltro.builder().build();
        Pageable expectedPageable = PageRequest.of(2, 12, Sort.by("nombre").ascending());
        Page<Jugador> pageMock = new PageImpl<>(Collections.emptyList(), expectedPageable, 0);

        when(repositorioJugador.buscarJugadoresConFiltro(eq(filtro), eq(expectedPageable))).thenReturn(pageMock);

        Page<Jugador> resultado = jugadorService.buscarJugadoresConFiltro(filtro, 2);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).isEmpty();
        verify(repositorioJugador).buscarJugadoresConFiltro(filtro, expectedPageable);
    }
}
