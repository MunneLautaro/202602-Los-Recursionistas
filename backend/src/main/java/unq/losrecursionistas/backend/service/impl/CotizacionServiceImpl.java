package unq.losrecursionistas.backend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unq.losrecursionistas.backend.model.Cotizacion;
import unq.losrecursionistas.backend.model.EstadisticaPartido;
import unq.losrecursionistas.backend.model.Jugador;
import unq.losrecursionistas.backend.controller.dto.cotizacion.CotizacionResponse;
import unq.losrecursionistas.backend.controller.dto.cotizacion.RankingJugadorResponse;
import unq.losrecursionistas.backend.exceptions.ExcepcionRecursoNoEncontrado;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;
import unq.losrecursionistas.backend.persistence.repository.interfaces.*;
import unq.losrecursionistas.backend.service.interfaces.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Transactional
public class CotizacionServiceImpl implements CotizacionService {
    private static final Logger log = LoggerFactory.getLogger(CotizacionServiceImpl.class);
    private final RepositorioJugador jugadores;
    private final RepositorioEstadisticaPartido estadisticas;
    private final RepositorioCotizacion cotizaciones;
    private final Map<String, EstrategiaCotizacion> estrategias;
    private final String estrategiaActiva;
    private final int ventanaDias;
    private final BigDecimal valorBase;
    private final BigDecimal factorEscala;
    private final AtomicBoolean recalculoActivo = new AtomicBoolean();

    public CotizacionServiceImpl(RepositorioJugador jugadores,
                                 RepositorioEstadisticaPartido estadisticas,
                                 RepositorioCotizacion cotizaciones,
                                 Map<String, EstrategiaCotizacion> estrategias,
                                 @Value("${cotizacion.estrategia.activa:PERFORMANCE_GENERAL}") String estrategiaActiva,
                                 @Value("${cotizacion.ventana-dias:7}") int ventanaDias,
                                 @Value("${cotizacion.valor-base:100}") BigDecimal valorBase,
                                 @Value("${cotizacion.factor-escala:900}") BigDecimal factorEscala) {
        this.jugadores = jugadores;
        this.estadisticas = estadisticas;
        this.cotizaciones = cotizaciones;
        this.estrategias = estrategias;
        this.estrategiaActiva = estrategiaActiva;
        this.ventanaDias = ventanaDias;
        this.valorBase = valorBase;
        this.factorEscala = factorEscala;
    }

    @Override
    public void recalcular() {
        LocalDate fin = LocalDate.now();
        recalcular(jugadores.obtenerJugadoresActivos(), fin.minusDays(ventanaDias), fin);
    }

    @Override
    public void recalcular(LocalDate inicio, LocalDate fin) {
        recalcular(jugadores.obtenerJugadoresActivos(), inicio, fin);
    }

    @Override
    public void recalcular(List<Jugador> jugadoresElegibles, LocalDate inicio, LocalDate fin) {
        EstrategiaCotizacion estrategia = estrategias.get(estrategiaActiva);
        if (estrategia == null || valorBase == null || factorEscala == null || inicio == null
                || fin == null || inicio.isAfter(fin)) {
            throw new IllegalStateException("La estrategia o ventana de cotizacion es invalida");
        }

        List<ResumenJugador> resumenes = new ArrayList<>();
        for (Jugador jugador : jugadoresElegibles) {
            if (!jugador.isActivo() || jugador.getIdWhoscored() == null || jugador.getId() == null) continue;
            List<EstadisticaPartido> datos = estadisticas.buscarPorJugadorYRango(jugador.getId(), inicio, fin);
            if (datos.isEmpty()) continue;
            resumenes.add(new ResumenJugador(jugador, resumir(datos)));
        }
        Map<String, Integer> maximos = maximos(resumenes);
        ParametrosCotizacion parametros = new ParametrosCotizacion(valorBase, factorEscala, Map.of());
        for (ResumenJugador item : resumenes) {
            try {
                BigDecimal valor = estrategia.calcular(item.resumen, item.jugador.getPosicion(), maximos, parametros);
                cotizaciones.guardar(Cotizacion.crear(item.jugador, valor, LocalDateTime.now(),
                        estrategiaActiva, inicio, fin));
                log.info("Cotización recalculada para jugador '{}' (ID: {}): ${}",
                        item.jugador.getNombre(), item.jugador.getId(), valor);
            } catch (RuntimeException ex) {
                log.error("No se pudo calcular la cotizacion del jugador {}", item.jugador.getId(), ex);
            }
        }
    }

    @Override
    public void asegurarCotizacionInicial(Jugador jugador) {
        if (jugador == null || !jugador.isActivo() || jugador.getId() == null) {
            return;
        }
        if (cotizaciones.vigente(jugador.getId()) == null) {
            BigDecimal valor = (valorBase != null && valorBase.signum() > 0) ? valorBase : BigDecimal.valueOf(100);
            LocalDate hoy = LocalDate.now();
            Cotizacion cotizacion = Cotizacion.crear(jugador, valor, LocalDateTime.now(),
                    "VALOR_BASE_INICIAL", hoy.minusDays(ventanaDias), hoy);
            cotizaciones.guardar(cotizacion);
            log.info("Cotización inicial base creada para el jugador '{}' (ID: {}): ${}",
                    jugador.getNombre(), jugador.getId(), valor);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CotizacionResponse> historial(Long jugadorId, LocalDate desde, LocalDate hasta,
                                              int pagina, int tamano) {
        validarPagina(pagina, tamano);
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ExcepcionValidacion("El rango de fechas es invalido");
        }
        if (!jugadores.existe(jugadorId)) {
            throw new ExcepcionRecursoNoEncontrado("No existe el jugador con el id: " + jugadorId);
        }
        var page = PageRequest.of(pagina, tamano,
                Sort.by(Sort.Order.desc("timestampCalculo"), Sort.Order.desc("id")));
        return cotizaciones.buscarPorJugador(jugadorId, desde, hasta, page)
                .map(CotizacionResponse::desde);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RankingJugadorResponse> ranking(Long ligaId, String posicion, int pagina, int tamano) {
        validarPagina(pagina, tamano);
        return cotizaciones.buscarRanking(ligaId, normalizarPosicion(posicion),
                        PageRequest.of(pagina, tamano))
                .map(r -> new RankingJugadorResponse(r.getJugadorId(), r.getNombre(), r.getPosicion(),
                        r.getEquipo(), r.getLiga(), r.getValorActual(), r.getValorAnterior(),
                        r.getVariacion()));
    }

    @Override
    public boolean reservarRecalculo() {
        return recalculoActivo.compareAndSet(false, true);
    }

    @Override
    public void liberarRecalculo() {
        recalculoActivo.set(false);
    }

    private void validarPagina(int pagina, int tamano) {
        if (pagina < 0 || tamano <= 0 || tamano > 100) {
            throw new ExcepcionValidacion("Los parametros de paginacion son invalidos");
        }
    }

    private String normalizarPosicion(String posicion) {
        if (posicion == null || posicion.isBlank()) return null;
        return java.text.Normalizer.normalize(posicion, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[\\s_-]", "");
    }

    private ResumenRendimiento resumir(List<EstadisticaPartido> datos) {
        return new ResumenRendimiento(
                datos.stream().mapToInt(d -> entero(d.getGoles())).sum(),
                datos.stream().mapToInt(d -> entero(d.getAsistencias())).sum(),
                datos.stream().mapToInt(d -> entero(d.getTiros())).sum(),
                datos.stream().mapToInt(d -> entero(d.getPasesClave())).sum(),
                datos.stream().mapToInt(d -> entero(d.getRegates())).sum(),
                datos.stream().mapToInt(d -> entero(d.getTackles())).sum(),
                datos.stream().mapToInt(d -> entero(d.getIntercepciones())).sum(),
                datos.stream().mapToInt(d -> entero(d.getMinutos())).sum(),
                datos.stream().map(d -> d.getRating() == null ? BigDecimal.ZERO : d.getRating())
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private Map<String, Integer> maximos(List<ResumenJugador> resumenes) {
        Map<String, Integer> resultado = new HashMap<>();
        resultado.put("goles", max(resumenes, r -> r.resumen.goles()));
        resultado.put("asistencias", max(resumenes, r -> r.resumen.asistencias()));
        resultado.put("tiros", max(resumenes, r -> r.resumen.tiros()));
        resultado.put("pasesClave", max(resumenes, r -> r.resumen.pasesClave()));
        resultado.put("regates", max(resumenes, r -> r.resumen.regates()));
        resultado.put("tackles", max(resumenes, r -> r.resumen.tackles()));
        resultado.put("intercepciones", max(resumenes, r -> r.resumen.intercepciones()));
        resultado.put("minutos", max(resumenes, r -> r.resumen.minutos()));
        resultado.put("rating", max(resumenes, r -> r.resumen.rating().intValue()));
        return resultado;
    }

    private int max(List<ResumenJugador> items, java.util.function.ToIntFunction<ResumenJugador> fn) {
        return items.stream().mapToInt(fn).max().orElse(0);
    }

    private int entero(Integer valor) { return valor == null ? 0 : valor; }

    private record ResumenJugador(Jugador jugador, ResumenRendimiento resumen) {}
}
