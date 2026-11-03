import { useEffect, useState, type FormEvent } from "react";
import { fetchJugadores } from "../../jugadores/api/jugadoresApi";
import type { JugadorResponseDTO } from "../../jugadores/types";
import type { OrderFilters as OrderFiltersValue, OrderType } from "../types";
import { orderFiltersStyles } from "./OrderFilters.styles";

interface OrderFiltersProps {
  filters: OrderFiltersValue;
  onChange: (filters: OrderFiltersValue) => void;
}

export function OrderFilters({ filters, onChange }: OrderFiltersProps) {
  const [query, setQuery] = useState(filters.jugadorNombre ?? "");
  const [players, setPlayers] = useState<JugadorResponseDTO[]>([]);
  const [loadingPlayers, setLoadingPlayers] = useState(false);

  useEffect(() => {
    if (query.trim().length < 2 || filters.jugadorId !== undefined) {
      return;
    }
    const timer = window.setTimeout(() => {
      setLoadingPlayers(true);
      void fetchJugadores({ nombre: query.trim() }, 0, 8)
        .then((response) => setPlayers(response.content))
        .catch(() => setPlayers([]))
        .finally(() => setLoadingPlayers(false));
    }, 300);
    return () => window.clearTimeout(timer);
  }, [filters.jugadorId, query]);

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    onChange(filters);
  };
  return (
    <form className={orderFiltersStyles.form} onSubmit={submit}>
      <label className={orderFiltersStyles.field}>
        Jugador
        <div className="relative">
          <input
            className={orderFiltersStyles.input}
            type="search"
            placeholder="Nombre o apellido"
            value={query}
            onChange={(event) => {
              setQuery(event.target.value);
              onChange({
                ...filters,
                jugadorId: undefined,
                jugadorNombre: event.target.value || undefined,
              });
            }}
          />
          {loadingPlayers && <small className="block">Buscando...</small>}
          {players.length > 0 && (
            <div className="absolute z-10 mt-1 grid w-full rounded border border-[var(--border)] bg-[var(--bg)] p-1 shadow-[var(--shadow)]">
              {players.map((player) => (
                <button
                  type="button"
                  key={player.id}
                  className="p-2 text-left hover:bg-[var(--accent-bg)]"
                  onClick={() => {
                    setQuery(player.nombre);
                    setPlayers([]);
                    onChange({ ...filters, jugadorId: player.id, jugadorNombre: undefined });
                  }}
                >
                  {player.nombre}{player.equipo ? ` - ${player.equipo.nombre}` : ""}
                </button>
              ))}
            </div>
          )}
        </div>
      </label>
      <label className={orderFiltersStyles.field}>
        Tipo
        <select
          className={orderFiltersStyles.input}
          value={filters.tipo ?? ""}
          onChange={(event) =>
            onChange({
              ...filters,
              tipo: (event.target.value || undefined) as OrderType | undefined,
            })
          }
        >
          <option value="">Todos</option>
          <option value="COMPRA">Compra</option>
          <option value="VENTA">Venta</option>
        </select>
      </label>
      <button className={orderFiltersStyles.button} type="submit">Aplicar</button>
    </form>
  );
}
