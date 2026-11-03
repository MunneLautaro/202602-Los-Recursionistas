export const headerStyles = {
  header:
    "sticky top-0 z-50 w-full border-b border-[var(--border)] bg-[var(--bg)]/95 shadow-sm backdrop-blur-md transition-colors duration-200",
  container:
    "mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-3 px-4 py-3 sm:px-6 lg:px-8",
  brandContainer: "flex min-w-0 items-center gap-3",
  logo: "h-10 w-10 rounded-xl object-cover shadow-sm ring-2 ring-[var(--pantone-sun-glare)]/60",
  title: "truncate text-lg font-bold tracking-tight text-[var(--text-h)] sm:text-xl",
  nav: "hidden items-center gap-1 md:flex",
  navLink:
    "rounded-lg px-3 py-2 text-sm font-semibold text-[var(--text)] transition-colors hover:bg-[var(--accent-bg)] hover:text-[var(--accent)] focus:outline-none focus:ring-2 focus:ring-[var(--accent)]",
  activeLink: "bg-[var(--accent-bg)] font-bold text-[var(--accent)]",
  mobileNav: "absolute left-0 right-0 top-full flex flex-col gap-1 border-b border-[var(--border)] bg-[var(--bg)] p-4 shadow-lg md:hidden",
  menuButton: "rounded-lg p-2 text-[var(--text)] transition-colors hover:bg-[var(--accent-bg)] focus:outline-none focus:ring-2 focus:ring-[var(--accent)] md:hidden",
  adminLink:
    "rounded-full bg-[var(--pantone-blue-violet)] px-3 py-1.5 text-xs font-bold text-[var(--pantone-cloud-dancer)] transition-transform hover:scale-105 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[var(--primary)]",
  actionsContainer: "flex items-center gap-2 sm:gap-3",
  userName: "hidden max-w-[150px] truncate text-sm font-semibold text-[var(--text-h)] lg:block",
  btnLogin:
    "inline-flex items-center gap-2 rounded-lg bg-[var(--pantone-sun-glare)] px-3 py-2 text-sm font-bold text-[var(--pantone-darkest-hour)] shadow-sm transition-transform hover:scale-105 active:scale-95 sm:px-4",
  btnRegister:
    "rounded-lg bg-[var(--pantone-exuberant-orange)] px-3 py-2 text-sm font-bold text-[var(--pantone-darkest-hour)] shadow-sm transition-transform hover:scale-105 active:scale-95 sm:px-4",
  btnLogout:
    "inline-flex items-center gap-2 rounded-lg border border-[var(--border)] bg-transparent px-3 py-2 text-sm font-bold text-[var(--text)] shadow-sm transition-all hover:border-[var(--pantone-exuberant-orange)] hover:bg-[var(--pantone-exuberant-orange)] hover:text-[var(--pantone-darkest-hour)] active:scale-95 sm:px-4",
} as const
