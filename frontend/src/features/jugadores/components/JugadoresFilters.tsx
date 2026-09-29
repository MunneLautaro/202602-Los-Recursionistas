import React, { useEffect, useState } from "react";
import { fetchEquiposPorLiga, fetchLigas } from "../api/catalogosApi";
import type {
  EquipoResponseDTO,
  JugadorFiltroRequestDTO,
  LigaResponseDTO,
} from "../types";
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
  const [ligas, setLigas] = useState<LigaResponseDTO[]>([]);
  const [equipos, setEquipos] = useState<EquipoResponseDTO[]>([]);
  const [loadingLigas, setLoadingLigas] = useState(true);
  const [loadingEquipos, setLoadingEquipos] = useState(false);
  const [catalogError, setCatalogError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;

    const loadLigas = async () => {
      try {
        const data = await fetchLigas();
        if (active) {
          setLigas(data);
        }
      } catch (e: unknown) {
        if (active) {
          setCatalogError(
            e instanceof Error ? e.message : "Error al cargar las ligas",
          );
        }
      } finally {
        if (active) {
          setLoadingLigas(false);
        }
      }
    };

    loadLigas();

    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    if (!local.ligaId) {
      return;
    }

    let active = true;

    const loadEquipos = async () => {
      try {
        const data = await fetchEquiposPorLiga(local.ligaId!);
        if (active) {
          setEquipos(data);
        }
      } catch (e: unknown) {
        if (active) {
          setEquipos([]);
          setCatalogError(
            e instanceof Error
              ? e.message
              : "Error al cargar los equipos de la liga",
          );
        }
      } finally {
        if (active) {
          setLoadingEquipos(false);
        }
      }
    };

    loadEquipos();

    return () => {
      active = false;
    };
  }, [local.ligaId]);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>,
  ) => {
    const { name, value } = e.target;
    const parsedValue = value
      ? isNaN(Number(value))
        ? value
        : Number(value)
      : undefined;

    if (name === "ligaId") {
      setEquipos([]);
      setLoadingEquipos(Boolean(parsedValue));
      setCatalogError(null);
    }

    setLocal((prev) => ({
      ...prev,
      [name]: parsedValue,
      ...(name === "ligaId" ? { equipoId: undefined } : {}),
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
      <select
        name="ligaId"
        value={local.ligaId ?? ""}
        onChange={handleChange}
        className={jugadoresFiltersStyles.input}
        disabled={loadingLigas}
      >
        <option value="">Todas las ligas</option>
        {ligas.map((liga) => (
          <option key={liga.id} value={liga.id}>
            {liga.nombre}
          </option>
        ))}
      </select>
      <select
        name="equipoId"
        value={local.equipoId ?? ""}
        onChange={handleChange}
        className={jugadoresFiltersStyles.input}
        disabled={!local.ligaId || loadingEquipos}
      >
        <option value="">
          {loadingEquipos ? "Cargando equipos..." : "Todos los equipos"}
        </option>
        {equipos.map((equipo) => (
          <option key={equipo.id} value={equipo.id}>
            {equipo.nombre}
          </option>
        ))}
      </select>
      <button type="submit" className={jugadoresFiltersStyles.button}>
        Aplicar
      </button>
      {catalogError && (
        <p className={jugadoresFiltersStyles.error}>{catalogError}</p>
      )}
    </form>
  );
};
