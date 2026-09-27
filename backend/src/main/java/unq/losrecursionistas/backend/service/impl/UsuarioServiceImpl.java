package unq.losrecursionistas.backend.service.impl;


import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioUsuario;


@Service
@Transactional
public class UsuarioService implements UsuarioService {

    private final RepositorioUsuario repositorioUsuario;

    public UsuarioService(RepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
    }



}
