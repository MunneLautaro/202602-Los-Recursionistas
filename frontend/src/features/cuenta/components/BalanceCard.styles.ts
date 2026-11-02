export const balanceCardStyles = {
  card: "rounded-xl border border-[var(--border)] bg-[var(--bg)] p-5 shadow-sm",
  title: "text-xl font-semibold text-[var(--text-h)]",
  values: "mt-4 grid gap-3 sm:grid-cols-3",
  value: "rounded-lg border border-[var(--border)] p-3",
  label: "block text-sm text-[var(--text)]",
  amount: "mt-1 text-xl font-bold text-[var(--accent)]",
} as const;
