export const jugadorDetailModalStyles = {
  overlay:
    "ui-overlay fixed inset-0 z-50 flex items-center justify-center p-4",
  modal:
    "ui-modal relative flex max-h-[90vh] w-full max-w-3xl flex-col overflow-y-auto p-6 text-left text-[var(--text)] outline-none sm:p-10",
  closeButton:
    "absolute right-5 top-5 grid h-10 w-10 place-items-center rounded-full border border-white/10 bg-white/[0.04] text-xl leading-none text-white/70 hover:bg-white/10 hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-mint/60",
  title:
    "mb-5 pr-12 text-3xl font-semibold tracking-[-0.03em] text-white sm:text-4xl",
  field:
    "mb-2 mr-2 inline-flex rounded-xl border border-white/[0.08] bg-white/[0.03] px-3 py-1.5 text-sm text-white/75",
  section: "mt-8 border-t border-white/[0.06] pt-6 text-left",
  subtitle: "ui-eyebrow mb-4 block",
  listItem:
    "border-b border-white/[0.05] px-3 py-3 text-sm text-white/70 last:border-b-0",
  filters: "mb-4 flex flex-wrap items-end gap-3",
  label: "flex min-w-36 flex-1 flex-col gap-2 text-sm font-medium text-white/60",
  input: "ui-input min-h-11 py-2",
  actionButton: "ui-btn-primary px-5 py-2.5 text-sm",
  pagination:
    "mt-4 flex items-center justify-between gap-2 text-sm text-white/55",
  error: "flex flex-wrap items-center gap-3 text-sm text-coral",
  tableWrapper:
    "overflow-x-auto rounded-2xl border border-white/[0.06] bg-white/[0.02] p-1",
  table: "w-full min-w-[36rem] border-collapse text-left text-sm tabular-nums",
  tableHeader:
    "border-b border-white/[0.08] px-3 py-3 text-[0.68rem] font-medium uppercase tracking-[0.2em] text-white/40",
  status: "rounded-2xl border border-white/[0.06] bg-white/[0.02] p-4 text-sm text-white/55",
} as const;
