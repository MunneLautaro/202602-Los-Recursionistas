package unq.losrecursionistas.backend.service.impl.exceptions;

import unq.losrecursionistas.backend.model.Usuario;

public class ExcepcionNombreDeUsuarioExistente extends unq.losrecursionistas.backend.exceptions.ExcepcionValidacion {
    public ExcepcionNombreDeUsuarioExistente(Usuario usuario) {
        super("El nombre de usuario ya existe: " + usuario.getNombreUsuario());
    }
}
