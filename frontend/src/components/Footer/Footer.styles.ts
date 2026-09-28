export const footerStyles = {
  footer:
    "w-full border-t border-[var(--border)] bg-[var(--bg)] py-8 transition-colors duration-200",
  container:
    "mx-auto flex max-w-7xl flex-col items-center justify-between gap-4 px-4 sm:flex-row sm:px-6 lg:px-8",
  brandSection: "flex items-center gap-2",
  brandTitle: "font-bold text-[var(--text-h)]",
  copyright: "text-sm text-[var(--text)]",
  nav: "flex items-center gap-6 text-sm",
  navLink:
    "text-[var(--text)] transition-colors hover:text-[var(--pantone-blue-violet)]",
} as const
