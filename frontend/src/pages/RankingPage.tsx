import { useCallback, useEffect, useState } from "react";
import { ArrowDown, ArrowUp, Minus } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { EstadoConsulta } from "../components";
import { fetchLigas } from "../features/jugadores/api/catalogosApi";
import type { LigaResponseDTO } from "../features/jugadores/types";
import { PaginationControls } from "../features/jugadores/components/PaginationControls";
import { fetchRanking } from "../features/mercado/api/rankingApi";
import { RankingFilters } from "../features/mercado/components/RankingFilters";
import type { RankingFilters as RankingFiltersValue, RankingPlayer } from "../features/mercado/types";

export function RankingPage() {
  const navigate = useNavigate();
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
      <EstadoConsulta
        estado={loading ? "cargando" : error ? "error" : players.length === 0 ? "vacio" : "exito"}
        mensaje={error ?? (players.length === 0 ? "No hay jugadores para los filtros seleccionados." : undefined)}
        accion={error ? { etiqueta: "Reintentar", ejecutar: () => void load(page) } : undefined}
      />
      {!loading && !error && players.length > 0 && (
        <>
          <div className="grid gap-3">
            {players.map((player) => (
              <button className="rounded border border-[var(--border)] p-4 text-left hover:border-[var(--accent)] focus:outline-none focus:ring-2 focus:ring-[var(--accent)]"
                key={player.jugadorId} onClick={() => navigate(`/jugadores?jugadorId=${player.jugadorId}`)}>
                <strong>{player.nombre}</strong>
                <span className="block text-sm">{player.posicion} · {player.equipo ?? "Sin equipo"} · {player.liga ?? "Sin liga"}</span>
                <span className="block">Actual: {player.valorActual} · Anterior: {player.valorAnterior ?? "N/D"}</span>
                <span className={`mt-2 inline-flex items-center gap-1 text-sm ${player.variacion === null ? "text-[var(--text)]" : player.variacion > 0 ? "text-green-700" : "text-[var(--secondary)]"}`}>
                  {player.variacion === null || player.variacion === 0 ? <Minus aria-hidden="true" size={16} /> : player.variacion > 0 ? <ArrowUp aria-hidden="true" size={16} /> : <ArrowDown aria-hidden="true" size={16} />}
                  {player.variacion === null ? "N/D (neutra)" : `${player.variacion > 0 ? "+" : ""}${player.variacion} (${player.variacion > 0 ? "positiva" : "negativa"})`}
                </span>
              </button>
            ))}
          </div>
          <PaginationControls page={page} totalPages={totalPages} onPageChange={load} />
        </>
      )}
    </main>
  );
}
