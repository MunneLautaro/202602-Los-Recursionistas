package unq.losrecursionistas.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unq.losrecursionistas.backend.controller.dto.cotizacion.CotizacionResponse;
import unq.losrecursionistas.backend.controller.dto.cotizacion.RankingJugadorResponse;
import unq.losrecursionistas.backend.exceptions.ExcepcionCotizacionActiva;
import unq.losrecursionistas.backend.service.impl.CotizacionAsyncWorker;
import unq.losrecursionistas.backend.service.interfaces.CotizacionService;

import java.time.LocalDate;
import java.util.Map;

@RestController
public class CotizacionController {
    private final CotizacionService servicio;
    private final CotizacionAsyncWorker worker;
    private final int ventanaDias;

    public CotizacionController(CotizacionService servicio, CotizacionAsyncWorker worker,
                                @Value("${cotizacion.ventana-dias:7}") int ventanaDias) {
        this.servicio = servicio;
        this.worker = worker;
        this.ventanaDias = ventanaDias;
    }

    @GetMapping("/players/{id}/quotes")
    @Operation(summary = "Historial de cotizaciones de un jugador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial paginado"),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos"),
            @ApiResponse(responseCode = "404", description = "Jugador inexistente")
    })
    public Page<CotizacionResponse> historial(@PathVariable Long id,
                             @RequestParam(required = false) LocalDate desde,
                             @RequestParam(required = false) LocalDate hasta,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "12") int size) {
        return servicio.historial(id, desde, hasta, page, size);
    }

    @GetMapping("/players/ranking")
    @Operation(summary = "Ranking paginado de jugadores por cotizacion actual")
    @ApiResponse(responseCode = "200", description = "Ranking paginado")
    public Page<RankingJugadorResponse> ranking(@RequestParam(required = false) Long ligaId,
                           @RequestParam(required = false) String posicion,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "12") int size) {
        return servicio.ranking(ligaId, posicion, page, size);
    }

    @PostMapping("/quotes/recalculate")
    @Operation(summary = "Acepta un recálculo asincrónico de cotizaciones")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Recálculo aceptado"),
            @ApiResponse(responseCode = "409", description = "Ya existe un recálculo activo"),
            @ApiResponse(responseCode = "500", description = "Configuración u operación invalida")
    })
    public ResponseEntity<Map<String, String>> recalcular() {
        if (!servicio.reservarRecalculo()) {
            throw new ExcepcionCotizacionActiva();
        }
        LocalDate fin = LocalDate.now();
        try {
            worker.ejecutar(fin.minusDays(ventanaDias), fin);
        } catch (RuntimeException ex) {
            servicio.liberarRecalculo();
            throw ex;
        }
        return ResponseEntity.accepted().body(Map.of("mensaje", "Recálculo de cotizaciones aceptado"));
    }
}
