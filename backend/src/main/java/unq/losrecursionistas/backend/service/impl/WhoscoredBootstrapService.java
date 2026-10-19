package unq.losrecursionistas.backend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unq.losrecursionistas.backend.adapter.whoscored.ScrapingConfiguration;
import unq.losrecursionistas.backend.adapter.whoscored.ScrapingPort;
import unq.losrecursionistas.backend.adapter.whoscored.dto.EquipoWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.JugadorWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.LigaWhoscoredDto;
import unq.losrecursionistas.backend.model.Equipo;
import unq.losrecursionistas.backend.model.Liga;
import unq.losrecursionistas.backend.model.EjecucionIngesta;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEquipo;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioJugador;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioLiga;
import unq.losrecursionistas.backend.service.interfaces.IngestaWhoscoredService;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class WhoscoredBootstrapService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhoscoredBootstrapService.class);
    private static final Set<String> LIGAS_INICIALES = Set.of(
            "premier league", "bundesliga", "la liga", "primera division", "serie a", "ligue 1");

    private final ScrapingPort scraping;
    private final ScrapingConfiguration configuracion;
    private final RepositorioLiga ligas;
    private final RepositorioEquipo equipos;
    private final RepositorioJugador jugadores;
    private final MatchingWhoscoredService matching;
    private final IngestaWhoscoredService ingesta;

    public WhoscoredBootstrapService(ScrapingPort scraping,
                                     ScrapingConfiguration configuracion,
                                     RepositorioLiga ligas,
                                     RepositorioEquipo equipos,
                                     RepositorioJugador jugadores,
                                     MatchingWhoscoredService matching,
                                     IngestaWhoscoredService ingesta) {
        this.scraping = scraping;
        this.configuracion = configuracion;
        this.ligas = ligas;
        this.equipos = equipos;
        this.jugadores = jugadores;
        this.matching = matching;
        this.ingesta = ingesta;
    }

    @Transactional
    public void cargarDatosIniciales() {
        List<LigaWhoscoredDto> descubiertas = scraping.descubrirLigas().stream()
                .filter(this::esLigaInicial)
                .toList();

        for (LigaWhoscoredDto descubierta : descubiertas) {
            Liga liga = encontrarLiga(descubierta);
            if (liga == null) {
                LOGGER.debug("No existe una liga local para WhoScored: {}", descubierta.nombre());
                continue;
            }
            liga.setIdWhoscored(descubierta.idWhoscored());
            ligas.guardar(liga);
            vincularEquipos(liga, descubierta.idWhoscored());
        }

        ingesta.iniciar(EjecucionIngesta.Alcance.COMPLETA, null);
    }

    private void vincularEquipos(Liga liga, Long ligaWhoscoredId) {
        List<Equipo> equiposLocales = equipos.obtenerEquiposDeLiga(liga.getId());
        for (EquipoWhoscoredDto descubierto : scraping.descubrirEquipos(ligaWhoscoredId)) {
            Equipo equipo = equiposLocales.stream()
                    .filter(local -> normalizar(local.getNombre()).equals(normalizar(descubierto.nombre())))
                    .findFirst()
                    .orElse(null);
            if (equipo == null) {
                continue;
            }
            equipo.setIdWhoscored(descubierto.idWhoscored());
            equipos.guardar(equipo);
            vincularJugadores(descubierto);
        }
    }

    private void vincularJugadores(EquipoWhoscoredDto equipo) {
        for (JugadorWhoscoredDto descubierto : scraping.descubrirJugadores(equipo.idWhoscored())) {
            MatchingWhoscoredService.ResultadoMatching resultado = matching.vincular(descubierto);
            if (resultado.estado() == MatchingWhoscoredService.Estado.VINCULADO) {
                jugadores.guardar(resultado.jugador());
            }
        }
    }

    private Liga encontrarLiga(LigaWhoscoredDto descubierta) {
        return ligas.obtenerLigas().stream()
                .filter(liga -> normalizar(liga.getNombre()).equals(normalizar(descubierta.nombre())))
                .findFirst()
                .orElse(null);
    }

    private boolean esLigaInicial(LigaWhoscoredDto liga) {
        return (configuracion.getLigasPermitidas().isEmpty()
                ? LIGAS_INICIALES.contains(normalizar(liga.nombre()))
                : configuracion.getLigasPermitidas().contains(liga.idWhoscored()));
    }

    private static String normalizar(String valor) {
        return Normalizer.normalize(valor == null ? "" : valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }
}
