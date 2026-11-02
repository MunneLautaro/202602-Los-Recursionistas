export const transactionHistoryStyles = {
  section: "grid gap-4",
  tableWrapper: "overflow-x-auto rounded-xl border border-[var(--border)]",
  table: "w-full min-w-[760px] text-left text-sm",
  header: "border-b border-[var(--border)] bg-[var(--code-bg)]",
  cell: "px-3 py-3",
  row: "border-b border-[var(--border)] last:border-b-0",
} as const;
