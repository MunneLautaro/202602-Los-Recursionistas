import { useEffect, useState } from "react";
import {
  adminApi,
  type IngestaAlcance,
  type IngestaStatus,
} from "../features/admin/api/adminApi";

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
      return state?.loading ? <p role="status">Procesando...</p> : null;
    return state.error ? (
      <p role="alert" className="text-sm text-[var(--secondary)]">
        {state.error}
      </p>
    ) : (
      <p
        role="status"
        className="text-sm text-emerald-700 dark:text-[var(--primary)]"
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
    <main className="mx-auto grid max-w-6xl gap-8 px-2 py-6 text-left">
      <header className="rounded-3xl bg-[var(--pantone-darkest-hour)] p-8 text-[var(--pantone-cloud-dancer)] shadow-xl">
        <p className="mb-2 text-sm font-bold uppercase tracking-[0.2em] text-[var(--pantone-sun-glare)]">
          Centro de control
        </p>
        <h1 className="m-0 text-4xl font-black text-white">Administración</h1>
        <p className="mt-3 max-w-2xl text-base text-white/75">
          Gestioná cotizaciones, sincronización e ingestas de datos desde un
          único lugar.
        </p>
      </header>

      <section
        className="grid gap-4 md:grid-cols-2"
        aria-labelledby="operaciones-title"
      >
        <div className="md:col-span-2">
          <h2 id="operaciones-title" className="text-2xl font-bold">
            Operaciones del sistema
          </h2>
          <p className="text-sm opacity-75">
            Estas tareas pueden tardar unos minutos y se ejecutan de forma
            segura.
          </p>
        </div>
        <article className="rounded-2xl border border-[var(--border)] bg-[var(--bg)] p-6 shadow-[var(--shadow)]">
          <p className="mb-2 text-3xl">↗</p>
          <h3 className="text-xl font-bold">Recalcular cotizaciones</h3>
          <p className="mb-5 mt-2 text-sm opacity-75">
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
            className="rounded-xl bg-[var(--primary)] px-4 py-2 font-bold text-[var(--pantone-darkest-hour)] disabled:opacity-50"
          >
            Recalcular
          </button>
          {renderFeedback("cotizaciones")}
        </article>
        <article className="rounded-2xl border border-[var(--border)] bg-[var(--bg)] p-6 shadow-[var(--shadow)]">
          <p className="mb-2 text-3xl">⇄</p>
          <h3 className="text-xl font-bold">Sincronizar jugadores</h3>
          <p className="mb-4 mt-2 text-sm opacity-75">
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
              className="min-w-0 flex-1 rounded-xl border border-[var(--border)] bg-transparent px-3 py-2"
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
              className="rounded-xl bg-[var(--accent)] px-4 py-2 font-bold text-[var(--accent-foreground)]"
            >
              Sincronizar
            </button>
          </div>
          {renderFeedback("sync")}
        </article>
        <article className="rounded-2xl border border-[var(--border)] bg-[var(--bg)] p-6 shadow-[var(--shadow)]">
          <p className="mb-2 text-3xl">⌁</p>
          <h3 className="text-xl font-bold">Vincular datos</h3>
          <p className="mb-5 mt-2 text-sm opacity-75">
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
            className="rounded-xl bg-[var(--accent)] px-4 py-2 font-bold text-[var(--accent-foreground)]"
          >
            Ejecutar linking
          </button>
          {renderFeedback("linking")}
        </article>
        <article className="rounded-2xl border border-[var(--border)] bg-[var(--bg)] p-6 shadow-[var(--shadow)]">
          <p className="mb-2 text-3xl">▦</p>
          <h3 className="text-xl font-bold">Ingestar estadísticas</h3>
          <p className="mb-5 mt-2 text-sm opacity-75">
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
            className="rounded-xl bg-[var(--accent)] px-4 py-2 font-bold text-[var(--accent-foreground)]"
          >
            Ejecutar ingesta
          </button>
          {renderFeedback("stats")}
        </article>
        <article className="rounded-2xl border border-[var(--border)] bg-[var(--bg)] p-6 shadow-[var(--shadow)]">
          <p className="mb-2 text-3xl">◎</p>
          <h3 className="text-xl font-bold">Estadísticas de jugador</h3>
          <p className="mb-4 mt-2 text-sm opacity-75">
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
              className="min-w-0 flex-1 rounded-xl border border-[var(--border)] bg-transparent px-3 py-2"
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
              className="rounded-xl bg-[var(--accent)] px-4 py-2 font-bold text-[var(--accent-foreground)]"
            >
              Ingestar
            </button>
          </div>
          {renderFeedback("playerStats")}
        </article>
      </section>

      <section
        className="rounded-2xl border border-[var(--border)] bg-[var(--bg)] p-6 shadow-[var(--shadow)]"
        aria-labelledby="whoscored-title"
      >
        <h2 id="whoscored-title" className="text-2xl font-bold">
          Ingesta WhoScored
        </h2>
        <p className="mb-5 mt-1 text-sm opacity-75">
          Iniciá una ejecución controlada por alcance y consultá su último
          estado.
        </p>
        <div className="flex flex-col gap-3 sm:flex-row">
          <select
            value={scope}
            onChange={(event) => setScope(event.target.value as IngestaAlcance)}
            className="rounded-xl border border-[var(--border)] bg-transparent px-3 py-2"
          >
            <option value="COMPLETA">Carga completa</option>
            <option value="LIGA">Por liga</option>
            <option value="EQUIPO">Por equipo</option>
            <option value="JUGADOR">Por jugador</option>
          </select>
          <button
            type="button"
            onClick={startIngesta}
            className="rounded-xl bg-[var(--secondary)] px-4 py-2 font-bold text-[var(--pantone-darkest-hour)]"
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
            className="mt-3 w-full rounded-xl border border-[var(--border)] bg-transparent px-3 py-2 sm:max-w-xs"
          />
        )}
        {renderFeedback("ingesta")}
      </section>

      <section
        className="rounded-2xl border border-[var(--border)] bg-[var(--bg)] p-6 shadow-[var(--shadow)]"
        aria-labelledby="estado-title"
      >
        <h2 id="estado-title" className="text-2xl font-bold">
          Última ingesta
        </h2>
        {lastIngestaError && (
          <p role="alert" className="mt-3 text-[var(--secondary)]">
            {lastIngestaError}
          </p>
        )}
        {!lastIngestaError && lastIngesta && (
          <div className="mt-4 grid gap-2 text-sm sm:grid-cols-3">
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
          <p className="mt-3 text-sm opacity-75">
            No hay ejecuciones registradas.
          </p>
        )}
      </section>
    </main>
  );
}
