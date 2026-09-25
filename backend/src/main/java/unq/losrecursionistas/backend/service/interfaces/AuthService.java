package unq.losrecursionistas.backend.service.interfaces;

import unq.losrecursionistas.backend.controller.dto.CredencialesLoginDto;
import unq.losrecursionistas.backend.controller.dto.RespuestaTokenDto;

public interface AuthService {

	RespuestaTokenDto login(CredencialesLoginDto credenciales);
}

