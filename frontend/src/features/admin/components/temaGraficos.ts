/**
 * Tokens visuales compartidos por los gráficos del panel de observabilidad.
 * Mismos valores que QuoteHistoryChart y que las variables del tema (index.css).
 */
export const COLOR_MINT = "#5cffd2";
export const COLOR_LAVANDA = "#a98bff";
export const COLOR_AMBAR = "#ffb36b";
export const COLOR_CORAL = "#ff7f7a";
export const COLOR_GRILLA = "rgba(255, 255, 255, 0.06)";
export const COLOR_EJES = "rgba(235, 238, 248, 0.7)";
export const COLOR_CURSOR = "rgba(255, 255, 255, 0.06)";

export const ESTILO_TOOLTIP = {
  backgroundColor: "rgba(12, 14, 21, 0.95)",
  border: "1px solid rgba(255, 255, 255, 0.14)",
  borderRadius: "0.75rem",
  color: "#fff",
  fontSize: "0.75rem",
} as const;

export const ESTILO_EJE = { fill: COLOR_EJES, fontSize: 11 } as const;

const formatoDiaCorto = new Intl.DateTimeFormat("es-AR", { day: "2-digit", month: "short" });
const formatoNumero = new Intl.NumberFormat("es-AR", { maximumFractionDigits: 2 });
const formatoCompacto = new Intl.NumberFormat("es-AR", { notation: "compact", maximumFractionDigits: 1 });
const formatoMoneda = new Intl.NumberFormat("es-AR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const formatoPorcentaje = new Intl.NumberFormat("es-AR", {
  style: "percent",
  minimumFractionDigits: 1,
  maximumFractionDigits: 1,
});
const formatoVariacion = new Intl.NumberFormat("es-AR", {
  style: "percent",
  maximumFractionDigits: 1,
  signDisplay: "exceptZero",
});
const formatoHora = new Intl.DateTimeFormat("es-AR", { hour: "2-digit", minute: "2-digit" });

/** "2026-11-24" → "24 nov" (sin desfase de zona horaria). */
export function formatearDia(dia: string): string {
  return formatoDiaCorto.format(new Date(`${dia}T00:00:00`));
}

export function formatearNumero(valor: number | null | undefined): string {
  return valor === null || valor === undefined ? "Sin datos" : formatoNumero.format(valor);
}

export function formatearCompacto(valor: number): string {
  return formatoCompacto.format(valor);
}

export function formatearCreditos(valor: number | null | undefined): string {
  return valor === null || valor === undefined ? "Sin datos" : `$ ${formatoMoneda.format(valor)}`;
}

export function formatearPorcentaje(valor: number | null | undefined): string {
  return valor === null || valor === undefined ? "Sin datos" : formatoPorcentaje.format(valor);
}

export function formatearVariacion(valor: number | null | undefined): string {
  return valor === null || valor === undefined ? "Sin datos" : formatoVariacion.format(valor);
}

export function formatearHora(instante: number): string {
  return formatoHora.format(new Date(instante));
}

export function formatearDuracion(ms: number | null | undefined): string {
  if (ms === null || ms === undefined) {
    return "Sin datos";
  }
  return ms < 1000 ? `${ms} ms` : `${formatoNumero.format(ms / 1000)} s`;
}

export function formatearUptime(segundos: number | null | undefined): string {
  if (segundos === null || segundos === undefined) {
    return "Sin datos";
  }
  const horas = Math.floor(segundos / 3600);
  const minutos = Math.floor((segundos % 3600) / 60);
  return `${horas}h ${minutos}m`;
}
