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
      <label className="grid gap-1">
        Jugador
        <input
          className="rounded border border-[var(--border)] bg-[var(--bg)] p-2"
          value={query}
          placeholder="Buscar por nombre o apellido"
          onChange={(event) => {
            setQuery(event.target.value);
            onSelect(null);
          }}
        />
      </label>
      {loading && <small>Buscando jugadores...</small>}
      {!selected && !loading && query.trim().length >= 2 && results.length === 0 && (
        <small>No se encontraron jugadores.</small>
      )}
      {results.length > 0 && (
        <div className="absolute z-10 mt-1 grid w-full gap-1 rounded border border-[var(--border)] bg-[var(--bg)] p-1 shadow-[var(--shadow)]">
          {results.map((player) => (
            <button
              type="button"
              key={player.id}
              className="rounded p-2 text-left text-[var(--text)] hover:bg-[var(--accent-bg)] focus-visible:outline focus-visible:outline-2 focus-visible:outline-[var(--accent)]"
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
