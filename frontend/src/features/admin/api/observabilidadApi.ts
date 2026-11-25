import { ApiError, fetchClient } from "../../../lib/fetchClient";
import type {
  PeriodoObservabilidad,
  ResumenNegocio,
  ResumenTecnico,
} from "../types";

interface EstadoSalud {
  status?: string;
}

export const observabilidadApi = {
  negocio: (dias: PeriodoObservabilidad) =>
    fetchClient<ResumenNegocio>(`/api/admin/observabilidad/negocio?dias=${dias}`),
  tecnico: () => fetchClient<ResumenTecnico>("/api/admin/observabilidad/tecnico"),
  /**
   * Estado de salud público. No depende de la autenticación, por lo que sirve para
   * detectar una caída de la base sin provocar un 401 que invalide la sesión.
   */
  saludPublica: async (): Promise<string> => {
    try {
      // Accept explícito: sin él Actuator responde application/vnd.spring-boot.actuator.v3+json
      // y fetchClient no lo interpreta como JSON.
      const salud = await fetchClient<EstadoSalud>("/actuator/health", {
        headers: { Accept: "application/json" },
      });
      return salud.status ?? "UNKNOWN";
    } catch (reason) {
      if (reason instanceof ApiError && reason.status === 503) {
        return (reason.details as EstadoSalud | undefined)?.status ?? "DOWN";
      }
      throw reason;
    }
  },
};
