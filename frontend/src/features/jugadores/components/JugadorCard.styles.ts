export const jugadorCardStyles = {
  card: "bg-[var(--bg)] border border-[var(--border)] rounded-lg shadow-sm hover:shadow-md transition-shadow cursor-pointer p-4",
  header: "flex justify-between items-center mb-2",
  name: "text-lg font-semibold text-[var(--text-h)]",
  position: "text-sm text-[var(--pantone-blue-violet)]",
  body: "text-sm text-[var(--text)]",
  age: "mt-1",
  team: "mt-1",
  league: "mt-1",
} as const;
