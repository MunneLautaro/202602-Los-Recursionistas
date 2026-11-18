export const headerStyles = {
  header:
    "sticky top-0 z-50 w-full border-b border-white/[0.04] bg-ink/45 backdrop-blur-md",
  container:
    "relative mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-4 px-4 py-4 sm:px-6 lg:px-10",
  brandContainer: "group flex min-w-0 items-center gap-3",
  logo: "h-11 w-11 shrink-0 drop-shadow-[0_0_14px_rgba(169,139,255,0.35)] transition duration-300 group-hover:rotate-[72deg] group-hover:drop-shadow-[0_0_18px_rgba(92,255,210,0.5)]",
  // Logotipo en dos líneas como la versión horizontal: "LOS" fino + "RECURSIONISTAS" en negrita
  title: "flex min-w-0 flex-col uppercase leading-none text-[#eef0fb]",
  titleLight: "text-[0.7rem] font-extralight tracking-[0.42em]",
  titleBold: "mt-1 truncate text-[0.95rem] font-bold tracking-[0.08em] sm:text-base",
  // Píldora contenedora de la navegación (como "En vivo / Mercados / Clubes / Cartera")
  nav: "hidden items-center gap-1 rounded-full border border-white/10 bg-white/[0.03] p-1.5 text-white/60 shadow-[inset_0_1px_0_rgba(255,255,255,0.05)] md:flex",
  navLink:
    "rounded-full px-4 py-2 text-sm font-medium transition-colors hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-mint/60 lg:px-5",
  activeLink:
    "bg-white/10 text-white shadow-[inset_0_1px_0_rgba(255,255,255,0.08)]",
  // Variantes max-md: para que el menú abierto siempre gane sobre "hidden" en mobile
  mobileNav:
    "max-md:absolute max-md:left-4 max-md:right-4 max-md:top-full max-md:mt-2 max-md:flex max-md:flex-col max-md:items-stretch max-md:rounded-3xl max-md:border-white/10 max-md:bg-ink-2/95 max-md:p-2 max-md:shadow-2xl",
  menuButton:
    "grid h-10 w-10 place-items-center rounded-full border border-white/10 bg-white/[0.03] text-white/80 transition-colors hover:bg-white/10 hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-mint/60 md:hidden",
  adminLink:
    "rounded-full border border-lavender/40 bg-lavender/10 px-4 py-2 text-xs font-semibold text-lavender transition hover:bg-lavender/20 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-lavender/60",
  actionsContainer: "flex items-center gap-2 text-white/60 sm:gap-3",
  // Píldora de usuario (como el saldo arriba a la derecha)
  userName:
    "hidden max-w-[180px] items-center gap-2 truncate rounded-full border border-white/10 bg-white/[0.03] px-4 py-2 text-sm font-medium text-white before:h-1.5 before:w-1.5 before:shrink-0 before:rounded-full before:bg-mint before:shadow-[0_0_10px_rgba(94,245,207,0.9)] before:content-[''] lg:inline-flex",
  btnLogin: "ui-btn-primary px-4 py-2 text-sm sm:px-5",
  btnRegister: "ui-btn-ghost px-4 py-2 text-sm sm:px-5",
  btnLogout:
    "ui-btn-ghost px-4 py-2 text-sm text-white/80 hover:border-amber/50! hover:text-amber sm:px-5",
} as const
