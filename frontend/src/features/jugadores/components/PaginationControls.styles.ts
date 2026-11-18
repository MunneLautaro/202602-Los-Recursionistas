export const paginationStyles = {
  container: "mt-8 flex flex-wrap items-center justify-center gap-3",
  button: "ui-btn-ghost px-4 py-2 text-sm",
  numbers:
    "flex gap-1 rounded-full border border-white/10 bg-white/[0.03] p-1 text-white/60",
  pageButton:
    "grid h-9 min-w-9 place-items-center rounded-full px-3 text-sm font-medium tabular-nums hover:bg-white/[0.06] hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-mint/60",
  active:
    "bg-gradient-to-r from-mint to-lavender font-semibold text-ink! shadow-[0_0_20px_-6px_rgba(94,245,207,0.8)]",
} as const;
