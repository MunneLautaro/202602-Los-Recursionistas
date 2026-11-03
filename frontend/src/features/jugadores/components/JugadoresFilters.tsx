import React, { useEffect, useState } from "react"
import { fetchEquiposPorLiga, fetchLigas } from "../api/catalogosApi";
import type {
  EquipoResponseDTO,
  JugadorFiltroRequestDTO,
  LigaResponseDTO,
} from "../types";
import { jugadoresFiltersStyles } from "./JugadoresFilters.styles";
import { useDebounce } from "../../../hooks/useDebounce"

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
  const debouncedNombre = useDebounce(local.nombre ?? "")

  useEffect(() => {
    if (debouncedNombre === (filters.nombre ?? "")) {
      return
    }
    setFilters((previous) => ({
      ...previous,
      nombre: debouncedNombre || undefined,
    }))
  }, [debouncedNombre, filters.nombre, setFilters])

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
    const ligaId = local.ligaId;

    let active = true;

    const loadEquipos = async () => {
      try {
        const data = await fetchEquiposPorLiga(ligaId);
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
      ? Number.isNaN(Number(value))
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
      <label className={jugadoresFiltersStyles.field}>
        <span className={jugadoresFiltersStyles.label}>Nombre</span>
        <input
        name="nombre"
        placeholder="Buscar por nombre"
        value={local.nombre || ""}
        onChange={handleChange}
        className={jugadoresFiltersStyles.input}
        />
      </label>
      <label className={jugadoresFiltersStyles.field}>
        <span className={jugadoresFiltersStyles.label}>Posición</span>
        <select name="posicion" className={jugadoresFiltersStyles.input} onChange={handleChange} value={local.posicion || ""}>
          <option value="">Todas</option>
          <option value="Defence">Defensa</option>
          <option value="Goalkeeper">Arquero</option>
          <option value="Midfield">Mediocampo</option>
          <option value="Offence">Ataque</option>
        </select>
      </label>
      <label className={jugadoresFiltersStyles.field}>
        <span className={jugadoresFiltersStyles.label}>Liga</span>
        <select name="ligaId" value={local.ligaId ?? ""} onChange={handleChange} className={jugadoresFiltersStyles.input} disabled={loadingLigas}>
          <option value="">Todas las ligas</option>
          {ligas.map((liga) => <option key={liga.id} value={liga.id}>{liga.nombre}</option>)}
        </select>
      </label>
      <label className={jugadoresFiltersStyles.field}>
        <span className={jugadoresFiltersStyles.label}>Equipo</span>
        <select name="equipoId" value={local.equipoId ?? ""} onChange={handleChange} className={jugadoresFiltersStyles.input} disabled={!local.ligaId || loadingEquipos}>
          <option value="">{loadingEquipos ? "Cargando equipos..." : "Todos los equipos"}</option>
          {equipos.map((equipo) => <option key={equipo.id} value={equipo.id}>{equipo.nombre}</option>)}
        </select>
      </label>
      <label className={jugadoresFiltersStyles.field}>
        <span className={jugadoresFiltersStyles.label}>Ordenar</span>
        <select name="orden" value={local.orden ?? ""} onChange={handleChange} className={jugadoresFiltersStyles.input}>
          <option value="">Predeterminado</option>
          <option value="nombre,asc">Nombre A-Z</option>
          <option value="nombre,desc">Nombre Z-A</option>
        </select>
      </label>
      <button type="submit" className={jugadoresFiltersStyles.button}>
        Aplicar
      </button>
      {catalogError && (
        <p className={jugadoresFiltersStyles.error}>{catalogError}</p>
      )}
    </form>
  )
}
