import { useEffect, useState } from "react";
import { fetchJugadores } from "../../jugadores/api/jugadoresApi";
import type { JugadorResponseDTO } from "../../jugadores/types";

interface PlayerSearchProps {
  selected: JugadorResponseDTO | null;
  onSelect: (player: JugadorResponseDTO | null) => void;
}

export function PlayerSearch({ selected, onSelect }: PlayerSearchProps) {
  const [query, setQuery] = useState(selected?.nombre ?? "");
  const [results, setResults] = useState<JugadorResponseDTO[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (selected || query.trim().length < 2) {
      return;
    }
    const timer = window.setTimeout(async () => {
      setLoading(true);
      try {
        const response = await fetchJugadores({ nombre: query.trim() }, 0, 8);
        setResults(response.content);
      } finally {
        setLoading(false);
      }
    }, 300);
    return () => window.clearTimeout(timer);
  }, [query, selected]);

  return (
    <div className="relative">
      <label className="grid gap-2 text-sm font-medium text-white/60">
        Jugador
        <input
          className="ui-input"
          value={query}
          placeholder="Buscar por nombre o apellido"
          onChange={(event) => {
            setQuery(event.target.value);
            onSelect(null);
          }}
        />
      </label>
      {loading && <small className="mt-1.5 block text-xs text-white/40">Buscando jugadores...</small>}
      {!selected && !loading && query.trim().length >= 2 && results.length === 0 && (
        <small className="mt-1.5 block text-xs text-white/40">No se encontraron jugadores.</small>
      )}
      {results.length > 0 && (
        <div className="absolute z-20 mt-2 grid max-h-64 w-full gap-1 overflow-y-auto rounded-2xl border border-white/10 bg-ink-2/95 p-1.5 shadow-2xl">
          {results.map((player) => (
            <button
              type="button"
              key={player.id}
              className="rounded-xl px-3 py-2.5 text-left text-sm text-white/80 hover:bg-white/[0.06] hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-mint/60"
              onClick={() => {
                setQuery(player.nombre);
                setResults([]);
                onSelect(player);
              }}
            >
              {player.nombre} {player.equipo ? `- ${player.equipo.nombre}` : ""}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
