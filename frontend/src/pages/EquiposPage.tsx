import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchEquiposPorLiga, fetchLigas } from "../features/jugadores/api/catalogosApi";
import type { EquipoResponseDTO, LigaResponseDTO } from "../features/jugadores/types";
import { EstadoConsulta } from "../components/EstadoConsulta"

interface LigaConEquipos extends LigaResponseDTO {
  equipos: EquipoResponseDTO[];
}

export function EquiposPage() {
  const navigate = useNavigate();
  const [ligas, setLigas] = useState<LigaConEquipos[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    const load = async () => {
      try {
        const ligasResponse = await fetchLigas();
        const conEquipos = await Promise.all(
          ligasResponse.map(async (liga) => ({
            ...liga,
            equipos: await fetchEquiposPorLiga(liga.id),
          })),
        );
        if (active) {
          setLigas(conEquipos);
          setError(null);
        }
      } catch (reason) {
        if (active) {
          setError(reason instanceof Error ? reason.message : "No se pudieron cargar los equipos.");
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };
    void load();
    return () => {
      active = false;
    };
  }, []);

  return (
    <main className="container mx-auto grid gap-6 p-4">
      <h1 className="text-2xl font-bold text-[var(--text-h)]">Equipos</h1>
      {loading && <EstadoConsulta estado="cargando" mensaje="Cargando equipos..." />}
      {!loading && error && (
        <EstadoConsulta
          estado="error"
          mensaje={error}
          accion={{ etiqueta: "Reintentar", ejecutar: () => window.location.reload() }}
        />
      )}
      {!loading && !error && ligas.length === 0 && (
        <EstadoConsulta estado="vacio" mensaje="No hay ligas disponibles." />
      )}
      {ligas.map((liga) => (
        <section key={liga.id} className="grid gap-3">
          <h2>{liga.nombre}</h2>
          {liga.equipos.length === 0 ? (
            <p>No hay equipos disponibles en esta liga.</p>
          ) : (
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {liga.equipos.map((equipo) => (
                <button
                  type="button"
                  key={equipo.id}
                  className="flex items-center gap-3 rounded-lg border border-[var(--border)] bg-[var(--accent-bg)] p-4 text-left shadow-[var(--shadow)] transition hover:-translate-y-0.5 hover:border-[var(--accent)] hover:bg-[var(--accent-bg)] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[var(--accent)]"
                  onClick={() => navigate(`/jugadores?equipoId=${equipo.id}`)}
                >
                  {equipo.escudoUrl ? (
                    <img src={equipo.escudoUrl} alt="" className="h-12 w-12 object-contain" />
                  ) : (
                    <span aria-hidden="true" className="grid h-12 w-12 place-items-center rounded-full bg-[var(--accent)] font-bold text-[var(--pantone-cloud-dancer)]">
                      {(equipo.sigla || equipo.nombreCorto || equipo.nombre).slice(0, 3).toUpperCase()}
                    </span>
                  )}
                  <span>
                    <strong className="block text-[var(--text-h)]">{equipo.nombre}</strong>
                    <small>{equipo.sigla || equipo.nombreCorto}</small>
                  </span>
                </button>
              ))}
            </div>
          )}
        </section>
      ))}
    </main>
  );
}
