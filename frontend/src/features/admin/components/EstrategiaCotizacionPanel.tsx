import { useState } from "react";
import { useEstrategiaCotizacion } from "../hooks/useEstrategiaCotizacion";
import { estrategiaPanelStyles as styles } from "./EstrategiaCotizacionPanel.styles";

function formatTimestamp(timestamp: string): string {
  return new Intl.DateTimeFormat("es-AR", {
    dateStyle: "medium",
    timeStyle: "short",
    timeZone: "America/Argentina/Buenos_Aires",
  }).format(new Date(timestamp));
}

export function EstrategiaCotizacionPanel() {
  const { estado, cargando, guardando, error, resultado, cambiar } =
    useEstrategiaCotizacion();
  const [seleccionada, setSeleccionada] = useState("");

  const confirmarCambio = () => {
    const estrategiaActual = seleccionada || estado?.estrategiaActiva;
    if (
      estrategiaActual &&
      estrategiaActual !== estado?.estrategiaActiva &&
      window.confirm(
        "La estrategia cambiará para los próximos recálculos. Las cotizaciones existentes no se recalcularán. ¿Continuar?",
      )
    ) {
      void cambiar(estrategiaActual);
    }
  };

  return (
    <section className={styles.card} aria-labelledby="estrategia-title">
      <h2 id="estrategia-title" className="text-2xl font-semibold tracking-tight text-white">
        Estrategia de cotización
      </h2>
      <p className="mt-1 text-sm text-white/50">
        Elegí el criterio que usarán los próximos recálculos.
      </p>
      {cargando && <p className="mt-4 text-sm text-white/60">Cargando estrategia...</p>}
      {error && <p role="alert" className="mt-4 text-sm text-coral">{error}</p>}
      {estado && (
        <>
          <div className="mt-5 grid gap-3 md:grid-cols-2">
            {estado.estrategias.map((estrategia) => (
              <label key={estrategia.codigo} className={styles.option}>
                <input
                  type="radio"
                  name="estrategia-cotizacion"
                  value={estrategia.codigo}
                  checked={(seleccionada || estado.estrategiaActiva) === estrategia.codigo}
                  onChange={() => setSeleccionada(estrategia.codigo)}
                  className="mr-3 accent-mint"
                />
                <span className="font-medium text-white">{estrategia.nombre}</span>
                <span className="mt-2 block text-sm text-white/55">{estrategia.descripcion}</span>
              </label>
            ))}
          </div>
          <button
            type="button"
            className="ui-btn-primary mt-5 px-6"
            disabled={guardando || seleccionada === estado.estrategiaActiva}
            onClick={confirmarCambio}
          >
            {guardando ? "Guardando..." : "Confirmar estrategia"}
          </button>
          {resultado && (
            <p role="status" className="mt-3 text-sm text-mint">
              {resultado.mensaje} {resultado.cambioAplicado ? "" : "No se registró un cambio porque ya estaba activa."}
            </p>
          )}
          <p className="mt-4 text-sm text-white/50">
            Origen: {estado.origen === "PERSISTIDA" ? "último cambio guardado" : "configuración inicial"}.
          </p>
          {estado.ultimoCambio && (
            <p className="mt-1 text-sm text-white/50">
              Último cambio por {estado.ultimoCambio.usuarioAdmin} el{" "}
              {formatTimestamp(estado.ultimoCambio.timestampCambio)}.
            </p>
          )}
        </>
      )}
    </section>
  );
}
