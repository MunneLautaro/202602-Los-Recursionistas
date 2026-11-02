import type { FormEvent } from "react";
import type { LigaResponseDTO } from "../../jugadores/types";
import type { RankingFilters as RankingFiltersValue } from "../types";
import { rankingFiltersStyles } from "./RankingFilters.styles";

interface RankingFiltersProps {
  filters: RankingFiltersValue;
  leagues: LigaResponseDTO[];
  onChange: (filters: RankingFiltersValue) => void;
}

export function RankingFilters({ filters, leagues, onChange }: RankingFiltersProps) {
  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    onChange(filters);
  };

  return (
    <form className={rankingFiltersStyles.form} onSubmit={submit}>
      <label className={rankingFiltersStyles.field}>
        Liga
        <select
          className={rankingFiltersStyles.input}
          value={filters.ligaId ?? ""}
          onChange={(event) =>
            onChange({
              ...filters,
              ligaId: event.target.value ? Number(event.target.value) : undefined,
            })
          }
        >
          <option value="">Todas</option>
          {leagues.map((league) => (
            <option key={league.id} value={league.id}>{league.nombre}</option>
          ))}
        </select>
      </label>
      <label className={rankingFiltersStyles.field}>
        Posición
        <select
          className={rankingFiltersStyles.input}
          value={filters.posicion ?? ""}
          onChange={(event) =>
            onChange({ ...filters, posicion: event.target.value || undefined })
          }
        >
          <option value="">Todas</option>
          <option value="Defence">Defence</option>
          <option value="Goalkeeper">Goalkeeper</option>
          <option value="Midfield">Midfield</option>
          <option value="Offence">Offence</option>
        </select>
      </label>
      <button className={rankingFiltersStyles.button} type="submit">Aplicar</button>
    </form>
  );
}
