export const estadoConsultaStyles = {
  container:
    "grid min-h-32 place-items-center gap-3 rounded-lg border border-[var(--border)] bg-[var(--accent-bg)] p-6 text-center",
  title: "text-lg font-semibold text-[var(--text-h)]",
  message: "text-sm text-[var(--text)]",
  action:
    "rounded-md bg-[var(--primary)] px-4 py-2 font-semibold text-[var(--pantone-darkest-hour)] transition hover:opacity-90 focus-visible:outline focus-visible:outline-2 focus-visible:outline-[var(--accent)] focus-visible:outline-offset-2",
  skeleton: "h-24 w-full animate-pulse rounded-lg bg-[var(--code-bg)]",
  error: "text-[var(--secondary)]",
} as const
