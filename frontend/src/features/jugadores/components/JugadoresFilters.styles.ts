export const jugadoresFiltersStyles = {
  form: "grid grid-cols-1 gap-3 rounded-lg border border-[var(--border)] bg-[var(--accent-bg)] p-4 sm:grid-cols-2 lg:grid-cols-5",
  field: "grid gap-1",
  label: "text-sm font-semibold text-[var(--text-h)]",
  input:
    "min-h-10 rounded-md border border-[var(--border)] bg-[var(--bg)] px-3 py-2 text-[var(--text)] outline-none focus-visible:ring-2 focus-visible:ring-[var(--accent)]",
  button:
    "self-end rounded-md bg-[var(--pantone-sun-glare)] px-4 py-2 font-semibold text-[var(--pantone-darkest-hour)] transition hover:opacity-90 focus-visible:outline focus-visible:outline-2 focus-visible:outline-[var(--accent)]",
  error: "basis-full text-sm text-[var(--pantone-exuberant-orange)]",
} as const;
