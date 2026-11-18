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
    <main className="grid gap-10">
      <header className="grid gap-3">
        <span className="ui-eyebrow flex items-center gap-3"><span className="ui-dot" aria-hidden="true" />Clubes</span>
        <h1 className="ui-page-title">Equipos</h1>
      </header>
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
        <section key={liga.id} className="grid gap-4">
          <h2 className="flex items-center gap-4 text-xl font-semibold text-white after:h-px after:flex-1 after:bg-white/[0.08] after:content-['']">{liga.nombre}</h2>
          {liga.equipos.length === 0 ? (
            <p className="text-sm text-white/50">No hay equipos disponibles en esta liga.</p>
          ) : (
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {liga.equipos.map((equipo) => (
                <button
                  type="button"
                  key={equipo.id}
                  className="ui-tile ui-tile-interactive flex items-center gap-4 p-4 text-left hover:-translate-y-0.5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-mint/60"
                  onClick={() => navigate(`/jugadores?equipoId=${equipo.id}`)}
                >
                  {equipo.escudoUrl ? (
                    <img src={equipo.escudoUrl} alt="" className="h-12 w-12 shrink-0 rounded-xl bg-white/[0.04] object-contain p-1.5" />
                  ) : (
                    <span aria-hidden="true" className="grid h-12 w-12 shrink-0 place-items-center rounded-xl bg-gradient-to-br from-mint to-lavender text-sm font-semibold text-ink">
                      {(equipo.sigla || equipo.nombreCorto || equipo.nombre).slice(0, 3).toUpperCase()}
                    </span>
                  )}
                  <span className="min-w-0">
                    <strong className="block truncate font-semibold text-white">{equipo.nombre}</strong>
                    <small className="text-xs uppercase tracking-[0.2em] text-white/45">{equipo.sigla || equipo.nombreCorto}</small>
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
