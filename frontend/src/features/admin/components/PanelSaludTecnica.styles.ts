export const panelSaludTecnicaStyles = {
  raiz: "grid gap-4",
  saludFila: "flex flex-wrap items-center gap-3",
  badgeUp: "inline-flex items-center gap-2 rounded-full border border-mint/40 bg-mint/10 px-3 py-1 text-sm font-semibold text-mint",
  badgeOtro: "inline-flex items-center gap-2 rounded-full border border-coral/40 bg-coral/10 px-3 py-1 text-sm font-semibold text-coral",
  componentes: "flex flex-wrap gap-2",
  componenteUp: "rounded-lg border border-white/10 bg-white/[0.03] px-2.5 py-1 text-xs text-white/70",
  componenteOtro: "rounded-lg border border-coral/40 bg-coral/10 px-2.5 py-1 text-xs font-medium text-coral",
  tarjetas: "grid grid-cols-2 gap-3 md:grid-cols-4",
  marco: "flex flex-col gap-3 rounded-2xl border border-white/[0.06] bg-white/[0.02] p-4",
  titulo: "text-sm font-medium text-white/70",
  lienzo: "h-80 w-full",
  vacio: "grid h-40 place-items-center text-sm text-white/40",
} as const;
