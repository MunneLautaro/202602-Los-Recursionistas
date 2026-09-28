export const jugadorDetailModalStyles = {
  overlay:
    "fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50",
  modal:
    "bg-[var(--bg)] text-[var(--text)] rounded-lg shadow-lg max-w-md w-full p-6 relative",
  closeButton:
    "absolute top-2 right-2 text-xl font-bold text-[var(--text)] hover:text-[var(--pantone-exuberant-orange)]",
  title: "text-2xl font-semibold mb-4 text-[var(--text-h)]",
  field: "mb-2 text-[var(--text)]",
} as const;
