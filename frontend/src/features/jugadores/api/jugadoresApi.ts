import { fetchClient } from "../../../lib/fetchClient";
import type {
  JugadorFiltroRequestDTO,
  PaginatedJugadores,
  JugadorResponseDTO,
} from "../types";

export async function fetchJugadores(
  filtros: JugadorFiltroRequestDTO = {},
  page = 0,
  size = 12,
): Promise<PaginatedJugadores> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
    ...Object.fromEntries(
      Object.entries(filtros)
        .filter(([, v]) => v !== undefined && v !== "")
        .map(([k, v]) => [k, String(v)]),
    ),
  });
  return await fetchClient<PaginatedJugadores>(`/players?${params}`);
}

export async function fetchJugadorDetalle(
  id: number,
): Promise<JugadorResponseDTO> {
  return await fetchClient<JugadorResponseDTO>(`/players/${id}`);
}
