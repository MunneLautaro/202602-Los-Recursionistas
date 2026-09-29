package unq.losrecursionistas.backend.service.impl.exceptions;

import unq.losrecursionistas.backend.model.Usuario;

public class ExcepcionNombreDeUsuarioExistente extends RuntimeException {
    public ExcepcionNombreDeUsuarioExistente(Usuario usuario) {
        super("El nombre de usuario ya existe: " + usuario.getNombreUsuario());
    }
}
