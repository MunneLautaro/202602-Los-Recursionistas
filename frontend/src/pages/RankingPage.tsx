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
    <main className="grid gap-6">
      <header className="grid gap-3">
        <span className="ui-eyebrow flex items-center gap-3"><span className="ui-dot" aria-hidden="true" />Cotizaciones</span>
        <h1 className="ui-page-title">Ranking del mercado</h1>
      </header>
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
              <button className="ui-tile ui-tile-interactive grid w-full gap-1 p-5 text-left focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-mint/60 md:grid-cols-[minmax(0,1fr)_auto_auto] md:items-center md:gap-x-8"
                key={player.jugadorId} onClick={() => navigate(`/jugadores?jugadorId=${player.jugadorId}`)}>
                <strong className="text-base font-semibold text-white md:col-start-1">{player.nombre}</strong>
                <span className="block text-sm text-white/50 md:col-start-1">{player.posicion} · {player.equipo ?? "Sin equipo"} · {player.liga ?? "Sin liga"}</span>
                <span className="block text-sm text-white/70 tabular-nums md:col-start-2 md:row-span-2 md:row-start-1 md:text-right">Actual: {player.valorActual} · Anterior: {player.valorAnterior ?? "N/D"}</span>
                <span className={`mt-2 inline-flex w-fit items-center gap-1.5 rounded-full px-3 py-1 text-sm font-medium tabular-nums md:col-start-3 md:row-span-2 md:row-start-1 md:mt-0 ${player.variacion === null ? "bg-white/[0.05] text-white/55" : player.variacion > 0 ? "bg-mint/10 text-mint" : "bg-amber/10 text-amber"}`}>
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
