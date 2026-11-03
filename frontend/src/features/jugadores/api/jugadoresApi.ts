import { fetchClient } from "../../../lib/fetchClient";
import type {
  JugadorFiltroRequestDTO,
  PaginatedJugadores,
  JugadorResponseDTO,
  PlayerStats,
  Quote,
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

export interface QuoteFilters {
  desde?: string;
  hasta?: string;
}

export async function fetchJugadorStats(id: number): Promise<PlayerStats> {
  return await fetchClient<PlayerStats>(`/players/${id}/stats`);
}

export async function fetchJugadorQuotes(
  id: number,
  filters: QuoteFilters = {},
  page = 0,
  size = 12,
): Promise<PaginatedResponse<Quote>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  });
  Object.entries(filters).forEach(([key, value]) => {
    if (value) {
      params.set(key, value);
    }
  });
  return await fetchClient<PaginatedResponse<Quote>>(
    `/players/${id}/quotes?${params}`,
  );
}

export interface PaginatedResponse<T> {
  content: T[];
  totalPages: number;
  number: number;
  size: number;
  totalElements: number;
}
