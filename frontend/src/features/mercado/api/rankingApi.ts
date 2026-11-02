import { fetchClient } from "../../../lib/fetchClient";
import type { PaginatedResponse, RankingFilters, RankingPlayer } from "../types";

export async function fetchRanking(
  filters: RankingFilters = {},
  page = 0,
  size = 12,
): Promise<PaginatedResponse<RankingPlayer>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== "") {
      params.set(key, String(value));
    }
  });
  return fetchClient<PaginatedResponse<RankingPlayer>>(
    `/players/ranking?${params}`,
  );
}
