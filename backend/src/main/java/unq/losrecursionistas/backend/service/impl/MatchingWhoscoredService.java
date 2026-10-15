package unq.losrecursionistas.backend.service.impl;

import org.springframework.stereotype.Service;
import unq.losrecursionistas.backend.adapter.whoscored.dto.JugadorWhoscoredDto;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioJugador;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MatchingWhoscoredService {
    private final RepositorioJugador repositorioJugador;

    public MatchingWhoscoredService(RepositorioJugador repositorioJugador) {
        this.repositorioJugador = repositorioJugador;
    }

    public ResultadoMatching buscar(JugadorWhoscoredDto descubierto) {
        if (descubierto == null || descubierto.idWhoscored() == null) {
            return ResultadoMatching.noEncontrado();
        }

        Jugador porId = repositorioJugador.buscarPorIdWhoscored(descubierto.idWhoscored());
        if (porId != null && porId.isActivo()) {
            return ResultadoMatching.vinculado(porId);
        }

        List<Jugador> candidatos = repositorioJugador.obtenerJugadoresActivos().stream()
                .filter(jugador -> coincideNombre(jugador, descubierto))
                .filter(jugador -> descubierto.fechaNacimiento() == null
                        || Objects.equals(descubierto.fechaNacimiento(), jugador.getFechaNacimiento()))
                .collect(Collectors.toList());

        if (candidatos.isEmpty()) {
            return ResultadoMatching.noEncontrado();
        }

        List<Jugador> porEquipo = candidatos.stream()
                .filter(jugador -> descubierto.equipoWhoscoredId() != null
                        && jugador.getEquipo() != null
                        && Objects.equals(descubierto.equipoWhoscoredId(),
                        jugador.getEquipo().getIdWhoscored()))
                .toList();
        if (!porEquipo.isEmpty()) {
            candidatos = porEquipo;
        }

        List<Jugador> porNacionalidad = candidatos.stream()
                .filter(jugador -> descubierto.nacionalidad() != null
                        && normalizar(jugador.getNacionalidad()).equals(normalizar(descubierto.nacionalidad())))
                .toList();
        if (!porNacionalidad.isEmpty()) {
            candidatos = porNacionalidad;
        }

        return candidatos.size() == 1
                ? ResultadoMatching.vinculado(candidatos.get(0))
                : ResultadoMatching.ambiguo(candidatos);
    }

    public ResultadoMatching vincular(JugadorWhoscoredDto descubierto) {
        ResultadoMatching resultado = buscar(descubierto);
        if (resultado.estado() == Estado.VINCULADO) {
            Jugador jugador = resultado.jugador();
            jugador.setIdWhoscored(descubierto.idWhoscored());
            if (!descubierto.posiciones().isEmpty()) {
                jugador.setPosicion(descubierto.posiciones().get(0));
            }
        }
        return resultado;
    }

    public static String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        String decodificado = URLDecoder.decode(valor, StandardCharsets.UTF_8);
        return Normalizer.normalize(decodificado, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean coincideNombre(Jugador jugador, JugadorWhoscoredDto descubierto) {
        return normalizar(jugador.getNombre()).equals(normalizar(descubierto.nombre()));
    }

    public record ResultadoMatching(Estado estado, Jugador jugador, List<Jugador> candidatos) {
        public ResultadoMatching {
            candidatos = candidatos == null ? List.of() : List.copyOf(candidatos);
        }

        public static ResultadoMatching vinculado(Jugador jugador) {
            return new ResultadoMatching(Estado.VINCULADO, jugador, List.of(jugador));
        }

        public static ResultadoMatching noEncontrado() {
            return new ResultadoMatching(Estado.NO_ENCONTRADO, null, List.of());
        }

        public static ResultadoMatching ambiguo(List<Jugador> candidatos) {
            return new ResultadoMatching(Estado.AMBIGUO, null, candidatos);
        }
    }

    public enum Estado {
        VINCULADO, NO_ENCONTRADO, AMBIGUO
    }
}
