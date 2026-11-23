import { useEffect, useState } from "react";
import {
  adminApi,
  type IngestaAlcance,
  type IngestaStatus,
} from "../features/admin/api/adminApi";
import { EstrategiaCotizacionPanel } from "../features/admin/components/EstrategiaCotizacionPanel";

interface ActionState {
  loading: boolean;
  message: string | null;
  error: string | null;
}

const initialActionState: ActionState = {
  loading: false,
  message: null,
  error: null,
};

function getErrorMessage(reason: unknown): string {
  return reason instanceof Error
    ? reason.message
    : "No se pudo completar la operación.";
}

export function AdminPage() {
  const [teamId, setTeamId] = useState("");
  const [playerId, setPlayerId] = useState("");
  const [scope, setScope] = useState<IngestaAlcance>("COMPLETA");
  const [lastIngesta, setLastIngesta] = useState<IngestaStatus | null>(null);
  const [lastIngestaError, setLastIngestaError] = useState<string | null>(null);
  const [actions, setActions] = useState<Record<string, ActionState>>({});

  useEffect(() => {
    void adminApi
      .ultimaIngesta()
      .then(setLastIngesta)
      .catch((reason: unknown) => setLastIngestaError(getErrorMessage(reason)));
  }, []);

  const runAction = async (
    key: string,
    action: () => Promise<unknown>,
    successMessage: string,
  ) => {
    setActions((current) => ({
      ...current,
      [key]: { ...initialActionState, loading: true },
    }));
    try {
      await action();
      setActions((current) => ({
        ...current,
        [key]: { loading: false, message: successMessage, error: null },
      }));
    } catch (reason: unknown) {
      setActions((current) => ({
        ...current,
        [key]: {
          loading: false,
          message: null,
          error: getErrorMessage(reason),
        },
      }));
    }
  };

  const idOrUndefined = (value: string): number | undefined => {
    const parsed = Number(value);
    return Number.isInteger(parsed) && parsed > 0 ? parsed : undefined;
  };

  const setValidationError = (key: string, message: string) => {
    setActions((current) => ({
      ...current,
      [key]: { loading: false, message: null, error: message },
    }));
  };

  const renderFeedback = (key: string) => {
    const state = actions[key];
    if (!state || state.loading)
      return state?.loading ? <p role="status" className="mt-3 text-sm text-white/50">Procesando...</p> : null;
    return state.error ? (
      <p role="alert" className="mt-3 text-sm text-coral">
        {state.error}
      </p>
    ) : (
      <p
        role="status"
        className="mt-3 text-sm text-mint"
      >
        {state.message}
      </p>
    );
  };

  const startIngesta = () => {
    const id = idOrUndefined(playerId);
    if (scope !== "COMPLETA" && id === undefined) {
      setActions((current) => ({
        ...current,
        ingesta: {
          loading: false,
          message: null,
          error: "Ingresá un ID válido para el alcance seleccionado.",
        },
      }));
      return;
    }
    void runAction(
      "ingesta",
      () =>
        adminApi.iniciarIngesta(scope, scope === "COMPLETA" ? undefined : id),
      "La ingesta fue aceptada y se ejecutará en segundo plano.",
    );
  };

  return (
    <main className="mx-auto grid max-w-6xl gap-8 text-left">
      <header className="ui-card overflow-hidden bg-[radial-gradient(60%_120%_at_0%_0%,rgba(98,56,222,0.42),transparent_70%),radial-gradient(50%_120%_at_100%_100%,rgba(158,86,38,0.32),transparent_70%)] p-8 sm:p-10">
        <p className="ui-eyebrow mb-4 flex items-center gap-3">
          <span className="ui-dot" aria-hidden="true" />
          Centro de control
        </p>
        <h1 className="ui-page-title">Administración</h1>
        <p className="mt-4 max-w-2xl text-base font-light text-white/60">
          Gestioná cotizaciones, sincronización e ingestas de datos desde un
          único lugar.
        </p>
      </header>

      <section
        className="grid gap-4 md:grid-cols-2"
        aria-labelledby="operaciones-title"
      >
        <div className="md:col-span-2">
          <h2 id="operaciones-title" className="text-2xl font-semibold tracking-tight text-white">
            Operaciones del sistema
          </h2>
          <p className="text-sm text-white/50">
            Estas tareas pueden tardar unos minutos y se ejecutan de forma
            segura.
          </p>
        </div>
        <article className="ui-card p-6 sm:p-8">
          <p className="mb-5 grid h-12 w-12 place-items-center rounded-2xl border border-white/10 bg-gradient-to-br from-mint/15 to-lavender/20 text-xl text-mint">↗</p>
          <h3 className="text-lg font-semibold tracking-tight text-white">Recalcular cotizaciones</h3>
          <p className="mb-5 mt-2 text-sm text-white/50">
            Actualiza el valor de mercado de todos los jugadores.
          </p>
          <button
            type="button"
            disabled={actions.cotizaciones?.loading}
            onClick={() =>
              void runAction(
                "cotizaciones",
                adminApi.recalcularCotizaciones,
                "El recálculo fue aceptado.",
              )
            }
            className="ui-btn-ghost px-5 py-2.5 text-sm"
          >
            Recalcular
          </button>
          {renderFeedback("cotizaciones")}
        </article>
        <article className="ui-card p-6 sm:p-8">
          <p className="mb-5 grid h-12 w-12 place-items-center rounded-2xl border border-white/10 bg-gradient-to-br from-mint/15 to-lavender/20 text-xl text-mint">⇄</p>
          <h3 className="text-lg font-semibold tracking-tight text-white">Sincronizar jugadores</h3>
          <p className="mb-4 mt-2 text-sm text-white/50">
            Importa los jugadores de un equipo externo.
          </p>
          <div className="flex gap-2">
            <input
              value={teamId}
              onChange={(event) => setTeamId(event.target.value)}
              type="number"
              min="1"
              placeholder="ID del equipo"
              aria-label="ID del equipo"
              className="ui-input min-w-0 flex-1"
            />
            <button
              type="button"
              onClick={() => {
                const id = idOrUndefined(teamId);
                if (id === undefined) {
                  setValidationError("sync", "Ingresá un ID de equipo válido.");
                  return;
                }
                void runAction(
                  "sync",
                  () => adminApi.sincronizarJugadores(id),
                  "La sincronización fue aceptada.",
                );
              }}
              className="ui-btn-ghost px-5 py-2.5 text-sm"
            >
              Sincronizar
            </button>
          </div>
          {renderFeedback("sync")}
        </article>
        <article className="ui-card p-6 sm:p-8">
          <p className="mb-5 grid h-12 w-12 place-items-center rounded-2xl border border-white/10 bg-gradient-to-br from-mint/15 to-lavender/20 text-xl text-mint">⌁</p>
          <h3 className="text-lg font-semibold tracking-tight text-white">Vincular datos</h3>
          <p className="mb-5 mt-2 text-sm text-white/50">
            Relaciona los registros de WhoScored con tus jugadores.
          </p>
          <button
            type="button"
            onClick={() =>
              void runAction(
                "linking",
                adminApi.ejecutarLinking,
                "El linking fue ejecutado.",
              )
            }
            className="ui-btn-ghost px-5 py-2.5 text-sm"
          >
            Ejecutar linking
          </button>
          {renderFeedback("linking")}
        </article>
        <article className="ui-card p-6 sm:p-8">
          <p className="mb-5 grid h-12 w-12 place-items-center rounded-2xl border border-white/10 bg-gradient-to-br from-mint/15 to-lavender/20 text-xl text-mint">▦</p>
          <h3 className="text-lg font-semibold tracking-tight text-white">Ingestar estadísticas</h3>
          <p className="mb-5 mt-2 text-sm text-white/50">
            Carga las estadísticas de todos los jugadores.
          </p>
          <button
            type="button"
            onClick={() =>
              void runAction(
                "stats",
                adminApi.ejecutarEstadisticas,
                "La ingesta de estadísticas fue ejecutada.",
              )
            }
            className="ui-btn-ghost px-5 py-2.5 text-sm"
          >
            Ejecutar ingesta
          </button>
          {renderFeedback("stats")}
        </article>
        <article className="ui-card p-6 sm:p-8">
          <p className="mb-5 grid h-12 w-12 place-items-center rounded-2xl border border-white/10 bg-gradient-to-br from-mint/15 to-lavender/20 text-xl text-mint">◎</p>
          <h3 className="text-lg font-semibold tracking-tight text-white">Estadísticas de jugador</h3>
          <p className="mb-4 mt-2 text-sm text-white/50">
            Actualiza un jugador puntual sin procesar todo el catálogo.
          </p>
          <div className="flex gap-2">
            <input
              value={playerId}
              onChange={(event) => setPlayerId(event.target.value)}
              type="number"
              min="1"
              placeholder="ID del jugador"
              aria-label="ID del jugador"
              className="ui-input min-w-0 flex-1"
            />
            <button
              type="button"
              onClick={() => {
                const id = idOrUndefined(playerId);
                if (id === undefined) {
                  setValidationError(
                    "playerStats",
                    "Ingresá un ID de jugador válido.",
                  );
                  return;
                }
                void runAction(
                  "playerStats",
                  () => adminApi.ejecutarEstadisticasJugador(id),
                  "Las estadísticas del jugador fueron ingestas.",
                );
              }}
              className="ui-btn-ghost px-5 py-2.5 text-sm"
            >
              Ingestar
            </button>
          </div>
          {renderFeedback("playerStats")}
        </article>
      </section>

      <EstrategiaCotizacionPanel />

      <section
        className="ui-card p-6 sm:p-8"
        aria-labelledby="whoscored-title"
      >
        <h2 id="whoscored-title" className="text-2xl font-semibold tracking-tight text-white">
          Ingesta WhoScored
        </h2>
        <p className="mb-5 mt-1 text-sm text-white/50">
          Iniciá una ejecución controlada por alcance y consultá su último
          estado.
        </p>
        <div className="flex flex-col gap-3 sm:flex-row">
          <select
            value={scope}
            onChange={(event) => setScope(event.target.value as IngestaAlcance)}
            className="ui-input sm:max-w-xs"
          >
            <option value="COMPLETA">Carga completa</option>
            <option value="LIGA">Por liga</option>
            <option value="EQUIPO">Por equipo</option>
            <option value="JUGADOR">Por jugador</option>
          </select>
          <button
            type="button"
            onClick={startIngesta}
            className="ui-btn-primary px-6"
          >
            Iniciar ingesta
          </button>
        </div>
        {scope !== "COMPLETA" && (
          <input
            value={playerId}
            onChange={(event) => setPlayerId(event.target.value)}
            type="number"
            min="1"
            placeholder="ID del alcance"
            aria-label="ID del alcance"
            className="ui-input mt-3 sm:max-w-xs"
          />
        )}
        {renderFeedback("ingesta")}
      </section>

      <section
        className="ui-card p-6 sm:p-8"
        aria-labelledby="estado-title"
      >
        <h2 id="estado-title" className="text-2xl font-semibold tracking-tight text-white">
          Última ingesta
        </h2>
        {lastIngestaError && (
          <p role="alert" className="mt-3 text-sm text-coral">
            {lastIngestaError}
          </p>
        )}
        {!lastIngestaError && lastIngesta && (
          <div className="mt-5 grid gap-3 text-sm text-white/65 sm:grid-cols-3 *:rounded-2xl *:border *:border-white/[0.08] *:bg-white/[0.025] *:p-4 [&_strong]:mr-1 [&_strong]:font-medium [&_strong]:text-white">
            <span>
              <strong>Estado:</strong> {lastIngesta.estado ?? "Sin información"}
            </span>
            <span>
              <strong>Alcance:</strong> {lastIngesta.alcance ?? "—"}
            </span>
            <span>
              <strong>ID:</strong> {lastIngesta.id ?? "—"}
            </span>
          </div>
        )}
        {!lastIngestaError && !lastIngesta && (
          <p className="mt-3 text-sm text-white/50">
            No hay ejecuciones registradas.
          </p>
        )}
      </section>
    </main>
  );
}
