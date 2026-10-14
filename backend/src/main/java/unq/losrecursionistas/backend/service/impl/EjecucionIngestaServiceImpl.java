package unq.losrecursionistas.backend.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import unq.losrecursionistas.backend.adapter.whoscored.ScrapingConfiguration;
import unq.losrecursionistas.backend.model.EjecucionIngesta;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEjecucionIngesta;
import unq.losrecursionistas.backend.service.interfaces.EjecucionIngestaService;

import java.time.OffsetDateTime;

@Service
public class EjecucionIngestaServiceImpl implements EjecucionIngestaService {
    private final RepositorioEjecucionIngesta repositorio;
    private final ScrapingConfiguration configuracion;

    public EjecucionIngestaServiceImpl(RepositorioEjecucionIngesta repositorio,
                                       ScrapingConfiguration configuracion) {
        this.repositorio = repositorio;
        this.configuracion = configuracion;
    }

    @Override
    @Transactional
    public EjecucionIngesta crearEjecucion(EjecucionIngesta.Alcance alcance, Long alcanceId) {
        if (repositorio.existeActiva()) {
            return repositorio.guardar(EjecucionIngesta.builder()
                    .estado(EjecucionIngesta.Estado.RECHAZADA)
                    .alcance(alcance)
                    .temporadaInicio(configuracion.getTemporadaInicio())
                    .temporadaFin(configuracion.getTemporadaFin())
                    .inicio(OffsetDateTime.now())
                    .fin(OffsetDateTime.now())
                    .motivoRechazo("Ya existe una ejecucion activa")
                    .build());
        }

        return repositorio.guardar(EjecucionIngesta.builder()
                .estado(EjecucionIngesta.Estado.ACTIVA)
                .alcance(alcance)
                .temporadaInicio(configuracion.getTemporadaInicio())
                .temporadaFin(configuracion.getTemporadaFin())
                .inicio(OffsetDateTime.now())
                .build());
    }
}
