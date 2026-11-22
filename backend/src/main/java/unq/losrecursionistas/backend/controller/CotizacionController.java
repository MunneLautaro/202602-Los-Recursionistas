package unq.losrecursionistas.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unq.losrecursionistas.backend.controller.dto.cotizacion.CotizacionResponse;
import unq.losrecursionistas.backend.controller.dto.cotizacion.PuntoCotizacionResponse;
import unq.losrecursionistas.backend.controller.dto.cotizacion.RankingJugadorResponse;
import unq.losrecursionistas.backend.exceptions.ExcepcionCotizacionActiva;
import unq.losrecursionistas.backend.service.interfaces.CotizacionService;
import unq.losrecursionistas.backend.service.interfaces.RecalculoCotizacionAsincrono;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@RestController
public class CotizacionController {
    private static final ZoneId ZONA_ARGENTINA = ZoneId.of("America/Argentina/Buenos_Aires");

    private final CotizacionService servicio;
    private final RecalculoCotizacionAsincrono recalculoAsincrono;
    private final int ventanaDias;

    public CotizacionController(CotizacionService servicio,
                                RecalculoCotizacionAsincrono recalculoAsincrono,
                                @Value("${cotizacion.ventana-dias:7}") int ventanaDias) {
        this.servicio = servicio;
        this.recalculoAsincrono = recalculoAsincrono;
        this.ventanaDias = ventanaDias;
    }

    @GetMapping("/players/{id}/quotes")
    @Operation(summary = "Historial de cotizaciones de un jugador")
    @ApiResponse(responseCode = "200", description = "Historial paginado")
    @ApiResponse(responseCode = "400", description = "Parametros invalidos")
    @ApiResponse(responseCode = "404", description = "Jugador inexistente")
    public Page<CotizacionResponse> historial(@PathVariable Long id,
                             @RequestParam(required = false) LocalDate desde,
                             @RequestParam(required = false) LocalDate hasta,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "12") int size) {
        return servicio.historial(id, desde, hasta, page, size).map(CotizacionResponse::desde);
    }

    @GetMapping("/players/{id}/quotes/series")
    @Operation(summary = "Serie temporal de cotizaciones de un jugador para graficar",
            description = "Devuelve hasta 500 puntos (los mas recientes del rango) ordenados "
                    + "cronologicamente, de la cotizacion mas antigua a la mas reciente.")
    @ApiResponse(responseCode = "200", description = "Serie de cotizaciones")
    @ApiResponse(responseCode = "400", description = "Rango de fechas invalido")
    @ApiResponse(responseCode = "404", description = "Jugador inexistente")
    public List<PuntoCotizacionResponse> serie(@PathVariable Long id,
                                               @RequestParam(required = false) LocalDate desde,
                                               @RequestParam(required = false) LocalDate hasta) {
        return servicio.serie(id, desde, hasta).stream()
                .map(PuntoCotizacionResponse::desde)
                .toList();
    }

    @GetMapping("/players/ranking")
    @Operation(summary = "Ranking paginado de jugadores por cotizacion actual")
    @ApiResponse(responseCode = "200", description = "Ranking paginado")
    public Page<RankingJugadorResponse> ranking(@RequestParam(required = false) Long ligaId,
                           @RequestParam(required = false) String posicion,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "12") int size) {
        return servicio.ranking(ligaId, posicion, page, size).map(RankingJugadorResponse::desde);
    }

    @PostMapping("/quotes/recalculate")
    @Operation(summary = "Acepta un recálculo asincrónico de cotizaciones")
    @ApiResponse(responseCode = "202", description = "Recálculo aceptado")
    @ApiResponse(responseCode = "409", description = "Ya existe un recálculo activo")
    @ApiResponse(responseCode = "500", description = "Configuración u operación invalida")
    public ResponseEntity<Map<String, String>> recalcular() {
        if (!servicio.reservarRecalculo()) {
            throw new ExcepcionCotizacionActiva();
        }
        LocalDate fin = LocalDate.now(ZONA_ARGENTINA);
        try {
            recalculoAsincrono.ejecutar(fin.minusDays(ventanaDias), fin);
        } catch (RuntimeException ex) {
            servicio.liberarRecalculo();
            throw ex;
        }
        return ResponseEntity.accepted().body(Map.of("mensaje", "Recálculo de cotizaciones aceptado"));
    }
}
