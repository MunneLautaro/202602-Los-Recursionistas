package unq.losrecursionistas.backend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unq.losrecursionistas.backend.model.Liga;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioLiga;
import unq.losrecursionistas.backend.service.interfaces.LigaService;

import java.util.List;

@Service
@Transactional
public class LigaServiceImpl implements LigaService {

    private final RepositorioLiga repositorioLiga;

    public LigaServiceImpl(RepositorioLiga repositorioLiga) {
        this.repositorioLiga = repositorioLiga;
    }

    @Override
    public List<Liga> obtenerLigas() {
        return repositorioLiga.obtenerLigas();
    }
}
