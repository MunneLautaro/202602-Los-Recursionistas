import { fetchClient } from "../../../lib/fetchClient"
import type {
  RegisterPayload,
  UsuarioResponse,
  LoginPayload,
  LoginResponse,
} from "../types"

export const authApi = {
  registrar: async (payload: RegisterPayload): Promise<UsuarioResponse> => {
    return fetchClient<UsuarioResponse>("/registrar", {
      method: "POST",
      body: JSON.stringify(payload),
    })
  },

  login: async (payload: LoginPayload): Promise<LoginResponse> => {
    return fetchClient<LoginResponse>("/login", {
      method: "POST",
      body: JSON.stringify(payload),
    })
  },
}
