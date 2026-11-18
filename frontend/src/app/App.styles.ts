export const appStyles = {
  heroContainer:
    "ui-card relative isolate flex flex-col sm:min-h-[calc(100svh-12rem)] gap-6 overflow-hidden px-6 pt-10 pb-6 text-left sm:px-10 sm:pt-12 lg:px-14",
  heroGlow:
    "pointer-events-none absolute inset-0 -z-10 bg-[radial-gradient(60%_50%_at_10%_0%,rgba(98,56,222,0.35),transparent_70%),radial-gradient(55%_55%_at_60%_75%,rgba(40,168,136,0.25),transparent_70%)]",
  eyebrow: "ui-eyebrow flex items-center gap-3",
  title:
    "max-w-4xl text-4xl font-semibold leading-[1.05] tracking-[-0.035em] text-white sm:text-6xl lg:text-7xl",
  brandHighlight: "ui-gradient-text",
  description:
    "max-w-2xl text-base font-light leading-relaxed text-white/60 sm:text-xl",
  pitchStage: "ui-pitch-stage relative mx-auto mt-6 w-full max-w-5xl px-2 sm:mt-auto sm:px-8 sm:pt-4",
  pitch: "ui-pitch",
  buttonContainer:
    "relative grid w-full grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-flow-col lg:auto-cols-fr lg:grid-cols-none",
  btnPrimary: "ui-btn-primary min-h-14 w-full text-base",
  btnSecondary:
    "ui-tile ui-tile-interactive inline-flex min-h-14 w-full items-center justify-between gap-3 px-5 py-3 text-sm font-medium text-white after:text-mint after:transition-transform after:content-['→'] hover:after:translate-x-1",
} as const;
