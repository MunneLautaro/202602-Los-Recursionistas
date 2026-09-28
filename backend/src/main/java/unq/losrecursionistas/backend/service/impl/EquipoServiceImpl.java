package unq.losrecursionistas.backend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unq.losrecursionistas.backend.model.Equipo;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioEquipo;
import unq.losrecursionistas.backend.service.interfaces.EquipoService;

@Service
@Transactional
public class EquipoServiceImpl implements EquipoService {

    private final RepositorioEquipo repositorioEquipo;

    public EquipoServiceImpl(RepositorioEquipo repositorioEquipo) {
        this.repositorioEquipo = repositorioEquipo;
    }

    @Override
    public Equipo guardar(Equipo equipo) {
        return repositorioEquipo.guardar(equipo);
    }

    @Override
    public Equipo buscarPorIdExterno(Long idExterno, Equipo equipo) {
        return repositorioEquipo.buscarPorIdExterno(idExterno, equipo);
    }

    @Override
    public Equipo guardarOActualizar(Equipo equipo) {
        return repositorioEquipo.guardarOActualizar(equipo);
    }
}
