export const paginationStyles = {
  container: "flex items-center justify-center gap-4 mt-6",
  button:
    "bg-[var(--pantone-sun-glare)] text-[var(--pantone-darkest-hour)] px-3 py-1 rounded hover:opacity-90 transition-opacity disabled:opacity-50 disabled:cursor-not-allowed",
  numbers: "flex gap-1",
  pageButton:
    "px-2 py-1 border border-[var(--border)] rounded hover:bg-[var(--pantone-cloud-dancer)]",
  active: "bg-[var(--pantone-blue-violet)] text-white",
} as const;
