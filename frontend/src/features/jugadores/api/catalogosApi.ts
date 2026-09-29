import { fetchClient } from "../../../lib/fetchClient";
import type { EquipoResponseDTO, LigaResponseDTO } from "../types";

export async function fetchLigas(): Promise<LigaResponseDTO[]> {
  return await fetchClient<LigaResponseDTO[]>("/leagues");
}

export async function fetchEquiposPorLiga(
  ligaId: number,
): Promise<EquipoResponseDTO[]> {
  return await fetchClient<EquipoResponseDTO[]>(`/teams/${ligaId}`);
}
