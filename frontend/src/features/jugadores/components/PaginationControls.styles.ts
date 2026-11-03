export const paginationStyles = {
  container: "flex items-center justify-center gap-4 mt-6",
  button:
    "rounded-lg bg-[var(--pantone-sun-glare)] px-3 py-1 text-[var(--pantone-darkest-hour)] transition hover:brightness-95 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[var(--accent)] disabled:cursor-not-allowed disabled:opacity-50",
  numbers: "flex gap-1",
  pageButton:
    "rounded-lg border border-[var(--border)] px-2 py-1 text-[var(--text)] hover:bg-[var(--accent-bg)] focus-visible:outline focus-visible:outline-2 focus-visible:outline-[var(--accent)]",
  active: "bg-[var(--pantone-blue-violet)] text-white",
} as const;
