package unq.losrecursionistas.backend.service.impl;


import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import unq.losrecursionistas.backend.model.Usuario;
import unq.losrecursionistas.backend.persistence.repository.interfaces.RepositorioUsuario;
import unq.losrecursionistas.backend.service.impl.exceptions.ExcepcionNombreDeUsuarioExistente;
import unq.losrecursionistas.backend.service.interfaces.UsuarioService;


@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final RepositorioUsuario repositorioUsuario;

    public UsuarioServiceImpl(RepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
    }


    @Override
    public Usuario crearUsuario(Usuario usuario) {
        if (existeUsuarioConMismoNombreDeUsuario(usuario)) {
            throw new ExcepcionNombreDeUsuarioExistente(usuario);
        }
        return repositorioUsuario.crearUsuario(usuario);
    }

    private boolean existeUsuarioConMismoNombreDeUsuario(Usuario usuario) {
        return repositorioUsuario.existeElUsuario(usuario.getNombreUsuario());
    }
}
