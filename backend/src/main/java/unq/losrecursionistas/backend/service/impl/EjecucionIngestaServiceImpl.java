package unq.losrecursionistas.backend.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import unq.losrecursionistas.backend.adapter.whoscored.ScrapingConfiguration;
import unq.losrecursionistas.backend.model.EjecucionIngesta;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEjecucionIngesta;
import unq.losrecursionistas.backend.service.interfaces.EjecucionIngestaService;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
public class EjecucionIngestaServiceImpl implements EjecucionIngestaService {
    private static final ZoneId ZONA_ARGENTINA = ZoneId.of("America/Argentina/Buenos_Aires");

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
        EjecucionIngesta ejecucion = EjecucionIngesta.builder()
                .estado(EjecucionIngesta.Estado.ACTIVA)
                .alcance(alcance)
                .inicio(OffsetDateTime.now(ZONA_ARGENTINA))
                .temporadaInicio(configuracion.getTemporadaInicio())
                .temporadaFin(configuracion.getTemporadaFin())
                .build();
        ejecucion.setIdAlcance(alcanceId);
        return repositorio.reservar(ejecucion);
    }

    @Override
    public EjecucionIngesta ultima() {
        return repositorio.ultima();
    }
}
