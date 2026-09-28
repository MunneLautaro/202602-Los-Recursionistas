import React, { useState } from "react";
import type { JugadorFiltroRequestDTO } from "../types";
import { jugadoresFiltersStyles } from "./JugadoresFilters.styles";

interface JugadoresFiltersProps {
  filters: JugadorFiltroRequestDTO;
  setFilters: React.Dispatch<React.SetStateAction<JugadorFiltroRequestDTO>>;
}

export const JugadoresFilters: React.FC<JugadoresFiltersProps> = ({
  filters,
  setFilters,
}) => {
  const [local, setLocal] = useState(filters);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>,
  ) => {
    const { name, value } = e.target;
    setLocal((prev) => ({
      ...prev,
      [name]: value
        ? isNaN(Number(value))
          ? value
          : Number(value)
        : undefined,
    }));
  };

  const applyFilters = (e: React.FormEvent) => {
    e.preventDefault();
    setFilters(local);
  };

  return (
    <form className={jugadoresFiltersStyles.form} onSubmit={applyFilters}>
      <input
        name="nombre"
        placeholder="Nombre"
        value={local.nombre || ""}
        onChange={handleChange}
        className={jugadoresFiltersStyles.input}
      />
      <select
        name="posicion"
        className={jugadoresFiltersStyles.input}
        onChange={handleChange}
        value={local.posicion || ""}
      >
        <option value="">Todas</option>
        <option value="Defence">Defence</option>
        <option value="Goalkeeper">Goalkeeper</option>
        <option value="Midfield">Midfield</option>
        <option value="Offence">Offence</option>
      </select>
      <input
        name="ligaId"
        placeholder="Liga ID"
        type="number"
        value={local.ligaId ?? ""}
        onChange={handleChange}
        className={jugadoresFiltersStyles.input}
      />
      <input
        name="equipoId"
        placeholder="Equipo ID"
        type="number"
        value={local.equipoId ?? ""}
        onChange={handleChange}
        className={jugadoresFiltersStyles.input}
      />
      <button type="submit" className={jugadoresFiltersStyles.button}>
        Aplicar
      </button>
    </form>
  );
};
