import { useCallback, useEffect, useState } from "react";
import { fetchLigas } from "../features/jugadores/api/catalogosApi";
import type { LigaResponseDTO } from "../features/jugadores/types";
import { PaginationControls } from "../features/jugadores/components/PaginationControls";
import { fetchRanking } from "../features/mercado/api/rankingApi";
import { RankingFilters } from "../features/mercado/components/RankingFilters";
import type { RankingFilters as RankingFiltersValue, RankingPlayer } from "../features/mercado/types";

export function RankingPage() {
  const [filters, setFilters] = useState<RankingFiltersValue>({});
  const [leagues, setLeagues] = useState<LigaResponseDTO[]>([]);
  const [players, setPlayers] = useState<RankingPlayer[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async (requestedPage: number) => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetchRanking(filters, requestedPage);
      setPlayers(response.content);
      setPage(response.number);
      setTotalPages(response.totalPages);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Error al cargar el ranking");
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      void load(0);
      void fetchLigas().then(setLeagues).catch(() => setLeagues([]));
    }, 0);
    return () => window.clearTimeout(timer);
  }, [load]);

  return (
    <main className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">Ranking del mercado</h1>
      <RankingFilters filters={filters} leagues={leagues} onChange={(next) => {
        setFilters(next);
        setPage(0);
      }} />
      {loading && <p>Cargando ranking...</p>}
      {error && <div className="text-[var(--secondary)]"><p>{error}</p><button onClick={() => load(page)}>Reintentar</button></div>}
      {!loading && !error && players.length === 0 && <p>No hay jugadores para los filtros seleccionados.</p>}
      {!loading && !error && players.length > 0 && (
        <>
          <div className="grid gap-3">
            {players.map((player) => (
              <button className="rounded border border-[var(--border)] p-4 text-left hover:border-[var(--accent)]"
                key={player.jugadorId} onClick={() => window.location.assign(`/jugadores?jugadorId=${player.jugadorId}`)}>
                <strong>{player.nombre}</strong>
                <span className="block text-sm">{player.posicion} · {player.equipo ?? "Sin equipo"} · {player.liga ?? "Sin liga"}</span>
                <span className="block">Actual: {player.valorActual} · Anterior: {player.valorAnterior ?? "N/D"} · Variación: {player.variacion ?? "N/D"}</span>
              </button>
            ))}
          </div>
          <PaginationControls page={page} totalPages={totalPages} onPageChange={load} />
        </>
      )}
    </main>
  );
}
