export const graficoRecursosStyles = {
  raiz: "grid gap-4",
  tarjetas: "grid grid-cols-2 gap-3 md:grid-cols-4",
  grilla: "grid gap-4 lg:grid-cols-2",
  marco: "flex flex-col gap-3 rounded-2xl border border-white/[0.06] bg-white/[0.02] p-4",
  titulo: "text-sm font-medium text-white/70",
  lienzo: "h-56 w-full",
  vacio: "grid h-56 place-items-center text-sm text-white/40",
  nota: "text-xs text-white/40",
} as const;
