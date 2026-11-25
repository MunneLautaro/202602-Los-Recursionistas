package unq.losrecursionistas.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import unq.losrecursionistas.backend.controller.dto.observabilidad.ResumenNegocioResponse;
import unq.losrecursionistas.backend.controller.dto.observabilidad.ResumenTecnicoResponse;
import unq.losrecursionistas.backend.exceptions.ExcepcionValidacion;
import unq.losrecursionistas.backend.service.interfaces.ObservabilidadService;

import java.util.Set;

@RestController
@RequestMapping("/api/admin/observabilidad")
@Tag(name = "Observabilidad", description = "Panel de observabilidad para administradores (ROLE_ADMIN)")
public class ObservabilidadAdminController {
    private static final Set<Integer> PERIODOS_VALIDOS = Set.of(7, 30, 90);

    private final ObservabilidadService servicio;

    public ObservabilidadAdminController(ObservabilidadService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/negocio")
    @Operation(summary = "Metricas de negocio de mercado y cotizacion con series diarias")
    @ApiResponse(responseCode = "200", description = "Resumen de negocio del periodo")
    @ApiResponse(responseCode = "400", description = "Periodo distinto de 7, 30 o 90 dias")
    @ApiResponse(responseCode = "401", description = "Sin autenticacion")
    @ApiResponse(responseCode = "403", description = "Requiere ROLE_ADMIN")
    public ResumenNegocioResponse negocio(@RequestParam(defaultValue = "30") int dias) {
        if (!PERIODOS_VALIDOS.contains(dias)) {
            throw new ExcepcionValidacion("El periodo debe ser 7, 30 o 90 dias");
        }
        return ResumenNegocioResponse.desde(servicio.resumenNegocio(dias));
    }

    @GetMapping("/tecnico")
    @Operation(summary = "Salud (sin detalles), requests por endpoint, JVM y pool de conexiones")
    @ApiResponse(responseCode = "200", description = "Instantanea tecnica")
    @ApiResponse(responseCode = "401", description = "Sin autenticacion")
    @ApiResponse(responseCode = "403", description = "Requiere ROLE_ADMIN")
    public ResumenTecnicoResponse tecnico() {
        return ResumenTecnicoResponse.desde(servicio.resumenTecnico());
    }
}
