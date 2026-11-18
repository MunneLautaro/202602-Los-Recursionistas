export const orderFiltersStyles = {
  form: "ui-card flex flex-1 flex-wrap items-end gap-4 p-5 sm:p-6",
  field: "flex min-w-48 flex-1 flex-col gap-2 text-xs font-medium text-white/50",
  input: "ui-input",
  button: "ui-btn-primary min-h-[2.875rem] px-6",
  suggestions:
    "absolute z-20 mt-2 grid w-full gap-1 rounded-2xl border border-white/10 bg-ink-2/95 p-1.5 shadow-2xl backdrop-blur-xl",
  suggestion:
    "rounded-xl px-3 py-2.5 text-left text-sm text-white/80 hover:bg-white/[0.06] hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-mint/60",
  hint: "mt-1.5 block text-xs text-white/40",
} as const;
