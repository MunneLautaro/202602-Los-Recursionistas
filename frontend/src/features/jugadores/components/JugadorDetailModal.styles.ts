export const jugadorDetailModalStyles = {
  overlay:
    "fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50",
  modal:
    "bg-[var(--bg)] text-[var(--text)] rounded-lg shadow-lg max-w-md w-full p-6 relative",
  closeButton:
    "absolute top-2 right-2 text-xl font-bold text-[var(--text)] hover:text-[var(--pantone-exuberant-orange)]",
  title: "text-2xl font-semibold mb-4 text-[var(--text-h)]",
  field: "mb-2 text-[var(--text)]",
  section: "mt-6 border-t border-[var(--border)] pt-4 text-left",
  subtitle: "text-lg font-semibold mb-2 text-[var(--text-h)]",
  listItem: "mb-1 text-sm text-[var(--text)]",
  filters: "flex flex-wrap items-end gap-2 mb-3",
  pagination: "flex items-center justify-between gap-2 mt-3 text-sm",
  error: "text-[var(--secondary)] flex flex-wrap items-center gap-2",
} as const;
