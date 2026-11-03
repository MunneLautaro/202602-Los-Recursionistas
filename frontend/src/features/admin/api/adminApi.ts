import { fetchClient } from "../../../lib/fetchClient";

export type IngestaAlcance = "COMPLETA" | "LIGA" | "EQUIPO" | "JUGADOR";

export interface AdminActionResponse {
  mensaje?: string;
  [key: string]: unknown;
}

export interface IngestaStatus {
  id?: number;
  estado?: string;
  alcance?: IngestaAlcance;
  idAlcance?: number;
  iniciadaEn?: string;
  finalizadaEn?: string;
  mensaje?: string;
}

export const adminApi = {
  recalcularCotizaciones: () =>
    fetchClient<AdminActionResponse>("/quotes/recalculate", { method: "POST" }),
  sincronizarJugadores: (equipoExternoId: number) =>
    fetchClient<void>(`/admin/sync/jugadores/${equipoExternoId}`, { method: "POST" }),
  ejecutarLinking: () =>
    fetchClient<AdminActionResponse>("/ingesta/linking", { method: "POST" }),
  ejecutarEstadisticas: () =>
    fetchClient<AdminActionResponse>("/ingesta/estadisticas", { method: "POST" }),
  ejecutarEstadisticasJugador: (jugadorId: number) =>
    fetchClient<AdminActionResponse>(`/ingesta/jugador/${jugadorId}/estadisticas`, { method: "POST" }),
  iniciarIngesta: (alcance: IngestaAlcance, id?: number) =>
    fetchClient<IngestaStatus>("/api/admin/whoscored/ingestas", {
      method: "POST",
      body: JSON.stringify({ alcance, id: id ?? null }),
    }),
  ultimaIngesta: () =>
    fetchClient<IngestaStatus>("/api/admin/whoscored/ingestas/ultima"),
};
