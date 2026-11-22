export const quoteHistoryChartStyles = {
  root: "flex flex-col gap-4",
  summary: "grid grid-cols-2 gap-2 sm:grid-cols-4",
  tile: "ui-tile flex flex-col gap-1 px-3 py-2.5",
  tileLabel: "text-[0.68rem] font-medium uppercase tracking-[0.2em] text-white/40",
  tileValue: "text-base font-semibold tabular-nums text-white",
  trendUp: "inline-flex items-center gap-1 text-base font-semibold tabular-nums text-mint",
  trendDown: "inline-flex items-center gap-1 text-base font-semibold tabular-nums text-coral",
  trendFlat: "inline-flex items-center gap-1 text-base font-semibold tabular-nums text-white/70",
  chartFrame:
    "h-72 w-full rounded-2xl border border-white/[0.06] bg-white/[0.02] px-1 pb-1 pt-4 sm:h-80",
  tooltip:
    "rounded-xl border border-white/[0.14] bg-[var(--color-ink-2)]/95 px-3 py-2 text-xs shadow-[0_18px_40px_-16px_rgba(0,0,0,0.9)] backdrop-blur",
  tooltipDate: "mb-1 text-white/50",
  tooltipValue: "text-sm font-semibold tabular-nums text-white",
  tooltipMeta: "mt-0.5 text-white/45",
  note: "text-xs text-white/40",
} as const;
