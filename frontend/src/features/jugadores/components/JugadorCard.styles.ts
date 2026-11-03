export const jugadorCardStyles = {
  card: "relative grid gap-3 rounded-lg border border-[var(--border)] bg-[var(--bg)] p-4 text-left shadow-sm transition hover:-translate-y-0.5 hover:shadow-md focus-visible:outline focus-visible:outline-2 focus-visible:outline-[var(--accent)]",
  header: "grid grid-cols-[auto_1fr_auto] items-center gap-2",
  avatar: "grid h-10 w-10 place-items-center rounded-full bg-[var(--accent)] text-sm font-bold text-[var(--pantone-cloud-dancer)]",
  name: "text-lg font-semibold text-[var(--text-h)]",
  position: "text-sm text-[var(--pantone-blue-violet)]",
  body: "text-sm text-[var(--text)]",
  age: "mt-1",
  team: "mt-1",
  league: "mt-1",
  icon: "absolute bottom-4 right-4 text-[var(--accent)]",
} as const;
