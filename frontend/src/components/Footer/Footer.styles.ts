export const footerStyles = {
  footer: "w-full border-t border-white/[0.05] py-8",
  container:
    "mx-auto flex max-w-7xl flex-col items-center justify-between gap-4 px-4 sm:flex-row sm:px-6 lg:px-10",
  brandSection: "flex flex-col items-center gap-3 sm:flex-row sm:gap-6",
  brandLockup: "flex items-center gap-3",
  brandLogo: "h-10 w-10 shrink-0 opacity-90",
  brandText: "grid gap-1.5",
  brandTitle:
    "text-xs font-bold uppercase tracking-[0.18em] text-[#eef0fb]",
  brandTagline:
    "flex items-center gap-2 text-[0.62rem] uppercase tracking-[0.36em] text-white/45 before:h-px before:w-5 before:bg-gradient-to-r before:from-mint before:to-lavender before:content-['']",
  copyright: "text-xs text-white/40",
  nav: "flex items-center gap-1 text-sm",
  navLink:
    "rounded-full px-3 py-1.5 text-white/50 transition-colors hover:bg-white/5 hover:text-white",
} as const
