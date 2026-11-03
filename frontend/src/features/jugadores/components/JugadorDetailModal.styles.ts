export const jugadorDetailModalStyles = {
  overlay:
    "fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4",
  modal:
    "relative flex max-h-[90vh] w-full max-w-3xl flex-col overflow-y-auto rounded-xl bg-[var(--bg)] p-5 text-[var(--text)] shadow-2xl sm:p-7",
  closeButton:
    "absolute right-3 top-3 rounded p-2 text-2xl leading-none text-[var(--text)] hover:bg-[var(--pantone-cloud-dancer)] focus:outline-none focus:ring-2 focus:ring-[var(--pantone-blue-violet)]",
  title: "mb-4 pr-10 text-2xl font-semibold text-[var(--text-h)] sm:text-3xl",
  field: "mb-2 text-[var(--text)]",
  section: "mt-6 border-t border-[var(--border)] pt-4 text-left",
  subtitle: "mb-3 text-lg font-semibold text-[var(--text-h)]",
  listItem: "border-b border-[var(--border)] px-2 py-2 text-sm text-[var(--text)] last:border-b-0",
  filters: "mb-3 flex flex-wrap items-end gap-3",
  label: "flex min-w-32 flex-1 flex-col gap-1 text-sm font-medium",
  input: "rounded border border-[var(--border)] bg-[var(--bg)] px-2 py-1 text-[var(--text)] focus:outline-none focus:ring-2 focus:ring-[var(--pantone-blue-violet)]",
  actionButton: "rounded bg-[var(--pantone-sun-glare)] px-3 py-2 font-semibold text-[var(--pantone-darkest-hour)] hover:opacity-90 focus:outline-none focus:ring-2 focus:ring-[var(--pantone-blue-violet)] disabled:cursor-not-allowed disabled:opacity-50",
  pagination: "mt-3 flex items-center justify-between gap-2 text-sm",
  error: "flex flex-wrap items-center gap-2 text-[var(--secondary)]",
  tableWrapper: "overflow-x-auto",
  table: "w-full min-w-[36rem] border-collapse text-left text-sm",
  tableHeader: "border-b border-[var(--border)] px-2 py-2 font-semibold",
  status: "rounded bg-[var(--pantone-cloud-dancer)] p-3 text-sm text-[var(--text)]",
} as const;
