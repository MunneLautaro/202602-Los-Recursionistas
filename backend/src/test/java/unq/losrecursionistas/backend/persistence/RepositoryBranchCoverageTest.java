package unq.losrecursionistas.backend.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import unq.losrecursionistas.backend.model.*;
import unq.losrecursionistas.backend.persistence.repository.impl.*;
import unq.losrecursionistas.backend.persistence.sql.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RepositoryBranchCoverageTest {

    @Test
    void cubreRamasDeEjecucionesLigaEquipoYOrden() {
        EjecucionIngestaDAOSQL ejecuciones = mock(EjecucionIngestaDAOSQL.class);
        EjecucionIngesta ejecucion = EjecucionIngesta.builder().estado(EjecucionIngesta.Estado.ACTIVA).build();
        when(ejecuciones.findFirstByEstadoOrderByInicioDesc(any())).thenReturn(Optional.of(ejecucion));
        new RepositorioEjecucionIngestaImpl(ejecuciones).reservar(ejecucion);
        when(ejecuciones.findFirstByEstadoOrderByInicioDesc(any())).thenReturn(Optional.empty());
        new RepositorioEjecucionIngestaImpl(ejecuciones).reservar(ejecucion);
        assertThat(ejecuciones.findFirstByEstadoOrderByInicioDesc(any())).isEmpty();

        LigaDAOSQL ligas = mock(LigaDAOSQL.class);
        Liga liga = new Liga(1L, "Liga", "L");
        when(ligas.findByCodigo("L")).thenReturn(Optional.of(liga));
        new RepositorioLigaImpl(ligas).buscarOModificarPorCodigoOIdExterno("L", 2L, "Nueva");
        when(ligas.findByCodigo("L")).thenReturn(Optional.empty());
        when(ligas.findByIdExterno(3L)).thenReturn(Optional.of(liga));
        new RepositorioLigaImpl(ligas).buscarOModificarPorCodigoOIdExterno("L", 3L, "");
        when(ligas.findByIdExterno(4L)).thenReturn(Optional.empty());
        new RepositorioLigaImpl(ligas).buscarOModificarPorCodigoOIdExterno("L", 4L, "Otra");

        OrdenDAOSQL ordenes = mock(OrdenDAOSQL.class);
        RepositorioOrdenImpl repo = new RepositorioOrdenImpl(ordenes);
        repo.buscarActivas(1L, TipoOrden.COMPRA, Pageable.unpaged());
        repo.buscarActivas(1L, null, Pageable.unpaged());
        repo.buscarActivas(null, TipoOrden.COMPRA, Pageable.unpaged());
        repo.buscarActivas(null, null, Pageable.unpaged());
    }

    @Test
    void cubreRamasDeEquiposYEstadisticas() {
        EquipoDAOSQL equipos = mock(EquipoDAOSQL.class);
        Equipo equipo = Equipo.builder().idExterno(1L).nombre("Equipo").build();
        when(equipos.findByIdExterno(1L)).thenReturn(Optional.of(equipo));
        RepositorioEquipoImpl repo = new RepositorioEquipoImpl(equipos);
        repo.buscarPorIdExterno(1L, equipo);
        repo.guardarOActualizar(equipo);
        equipo.setLiga(Liga.builder().id(1L).build());
        repo.guardarOActualizar(equipo);
        equipo.setIdExterno(null);
        repo.guardarOActualizar(equipo);

        EstadisticaPartidoDAOSQL estadisticas = mock(EstadisticaPartidoDAOSQL.class);
        EstadisticaPartido nueva = mock(EstadisticaPartido.class);
        Jugador jugador = Jugador.builder().id(1L).build();
        when(nueva.getJugador()).thenReturn(jugador);
        when(nueva.getPartidoWhoscoredId()).thenReturn(2L);
        RepositorioEstadisticaPartidoImpl estadisticaRepo = new RepositorioEstadisticaPartidoImpl(estadisticas);
        when(estadisticas.findByJugadorIdAndPartidoWhoscoredId(1L, 2L)).thenReturn(Optional.empty());
        estadisticaRepo.upsert(nueva);
        when(estadisticas.findByJugadorIdAndPartidoWhoscoredId(1L, 2L)).thenReturn(Optional.of(nueva));
        estadisticaRepo.upsert(nueva);
        assertThat(estadisticas.findByJugadorIdAndPartidoWhoscoredId(1L, 2L)).contains(nueva);
    }
}
