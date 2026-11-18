export const jugadorCardStyles = {
  card: "ui-tile ui-tile-interactive group relative grid gap-4 overflow-hidden rounded-3xl p-5 text-left hover:-translate-y-0.5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-mint/60",
  header: "grid grid-cols-[auto_1fr_auto] items-center gap-3",
  avatar:
    "grid h-11 w-11 place-items-center rounded-full bg-gradient-to-br from-mint to-lavender text-sm font-semibold text-ink shadow-[0_0_24px_-6px_rgba(92,255,210,0.7)]",
  name: "truncate text-lg font-semibold tracking-tight text-white",
  position:
    "rounded-full border border-lavender/30 bg-lavender/10 px-2.5 py-1 text-[0.68rem] font-medium uppercase tracking-[0.16em] text-lavender",
  body: "grid gap-1.5 border-t border-white/[0.06] pt-4 pr-8 text-sm text-white/55",
  age: "",
  team: "truncate",
  league: "truncate",
  icon: "absolute bottom-5 right-5 text-white/30 transition-all duration-200 group-hover:translate-x-1 group-hover:text-mint",
} as const;
