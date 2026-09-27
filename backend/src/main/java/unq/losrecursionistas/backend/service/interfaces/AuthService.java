package unq.losrecursionistas.backend.service.interfaces;

import unq.losrecursionistas.backend.controller.dto.CredencialesLoginDto;
import unq.losrecursionistas.backend.controller.dto.RespuestaTokenDto;
import unq.losrecursionistas.backend.model.Usuario;

public interface AuthService {

	Usuario registrarUsuario(Usuario usuario);

	RespuestaTokenDto login(CredencialesLoginDto credenciales);
}

