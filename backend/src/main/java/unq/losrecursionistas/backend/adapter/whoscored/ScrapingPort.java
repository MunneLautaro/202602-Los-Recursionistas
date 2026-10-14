package unq.losrecursionistas.backend.adapter.whoscored;

import unq.losrecursionistas.backend.adapter.whoscored.dto.AparicionWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.EquipoWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.JugadorWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.LigaWhoscoredDto;

import java.util.List;

public interface ScrapingPort {
    List<LigaWhoscoredDto> descubrirLigas();

    List<EquipoWhoscoredDto> descubrirEquipos(Long ligaWhoscoredId);

    List<JugadorWhoscoredDto> descubrirJugadores(Long equipoWhoscoredId);

    List<AparicionWhoscoredDto> obtenerApariciones(Long jugadorWhoscoredId);
}
