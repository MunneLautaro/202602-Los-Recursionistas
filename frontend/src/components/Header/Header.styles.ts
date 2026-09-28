export const headerStyles = {
  header:
    "sticky top-0 z-50 w-full border-b border-[var(--border)] bg-[var(--bg)]/90 backdrop-blur-md transition-colors duration-200",
  container:
    "mx-auto flex max-w-7xl items-center justify-between px-4 py-3 sm:px-6 lg:px-8",
  brandContainer: "flex items-center gap-3",
  logo: "flex h-10 w-10 items-center justify-center rounded-xl bg-[var(--pantone-sun-glare)] font-extrabold text-[var(--pantone-darkest-hour)] shadow-sm",
  title: "text-xl font-bold tracking-tight text-[var(--text-h)]",
  nav: "hidden items-center gap-6 md:flex",
  navLink:
    "text-sm font-medium text-[var(--text)] transition-colors hover:text-[var(--pantone-blue-violet)]",
  actionsContainer: "flex items-center gap-3",
  btnLogin:
    "rounded-lg bg-[var(--pantone-sun-glare)] px-4 py-2 text-sm font-semibold text-[var(--pantone-darkest-hour)] shadow-sm transition-transform hover:scale-105 active:scale-95",
  btnRegister:
    "rounded-lg bg-[var(--pantone-exuberant-orange)] px-4 py-2 text-sm font-semibold text-white shadow-sm transition-transform hover:scale-105 active:scale-95",
} as const
