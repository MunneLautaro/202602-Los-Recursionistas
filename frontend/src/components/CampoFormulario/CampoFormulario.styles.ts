export const campoFormularioStyles = {
  container: "grid gap-1",
  label: "text-sm font-semibold text-[var(--text-h)]",
  input:
    "w-full rounded-md border border-[var(--border)] bg-[var(--bg)] px-3 py-2 text-[var(--text)] outline-none transition focus-visible:ring-2 focus-visible:ring-[var(--accent)] focus-visible:ring-offset-2 focus-visible:ring-offset-[var(--bg)] disabled:cursor-not-allowed disabled:opacity-60",
  invalid:
    "border-[var(--secondary)] focus-visible:ring-[var(--secondary)]",
  help: "text-sm text-[var(--text)] opacity-75",
  error: "text-sm font-medium text-[var(--secondary)]",
  suffix: "flex items-center gap-2",
} as const
