export const appStyles = {
  heroContainer:
    "relative isolate flex min-h-[calc(100svh-5rem)] flex-col items-center justify-center gap-6 overflow-hidden px-5 py-16 text-center sm:px-8",
  title:
    "max-w-5xl text-4xl font-extrabold leading-tight tracking-tight text-[var(--text-h)] sm:text-6xl",
  brandHighlight:
    "bg-gradient-to-r from-[var(--pantone-blue-violet)] to-[var(--pantone-exuberant-orange)] bg-clip-text text-transparent",
  description:
    "max-w-2xl text-base leading-relaxed text-[var(--text)] sm:text-xl",
  buttonContainer:
    "mt-3 grid w-full max-w-3xl grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3",
  btnPrimary:
    "inline-flex min-h-12 items-center justify-center rounded-xl bg-[var(--pantone-sun-glare)] px-6 py-3 font-bold text-[var(--pantone-darkest-hour)] shadow-lg shadow-black/10 transition-all hover:-translate-y-0.5 hover:brightness-95 active:translate-y-0 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[var(--pantone-blue-violet)]",
  btnSecondary:
    "inline-flex min-h-12 items-center justify-center rounded-xl border border-[var(--pantone-exuberant-orange)] bg-[var(--pantone-exuberant-orange)] px-6 py-3 font-bold text-[var(--pantone-darkest-hour)] shadow-lg shadow-black/10 transition-all hover:-translate-y-0.5 hover:brightness-95 active:translate-y-0 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[var(--pantone-exuberant-orange)]",
} as const;
