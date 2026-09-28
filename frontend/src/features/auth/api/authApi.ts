import { fetchClient } from "../../../lib/fetchClient"
import type { RegisterPayload, UsuarioResponse } from "../types"

export const authApi = {
  registrar: async (payload: RegisterPayload): Promise<UsuarioResponse> => {
    return fetchClient<UsuarioResponse>("/registrar", {
      method: "POST",
      body: JSON.stringify(payload),
    })
  },
}
