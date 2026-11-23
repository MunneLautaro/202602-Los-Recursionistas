import { fetchClient } from "../../../lib/fetchClient";
import type {
  EstadoEstrategia,
  ResultadoCambioEstrategia,
} from "../types";

export const estrategiaApi = {
  consultar: () =>
    fetchClient<EstadoEstrategia>("/api/admin/cotizacion/estrategia"),
  cambiar: (estrategia: string) =>
    fetchClient<ResultadoCambioEstrategia>("/api/admin/cotizacion/estrategia", {
      method: "PUT",
      body: JSON.stringify({ estrategia }),
    }),
};
