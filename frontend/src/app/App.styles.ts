export const appStyles = {
  heroContainer: "flex flex-col items-center justify-center gap-4 py-12 text-center",
  title: "text-4xl font-extrabold text-[var(--text-h)] sm:text-5xl",
  brandHighlight: "text-[var(--pantone-blue-violet)]",
  description: "max-w-2xl text-lg text-[var(--text)]",
  buttonContainer: "mt-4 flex gap-4",
  btnPrimary: "rounded-lg bg-[var(--pantone-sun-glare)] px-6 py-3 font-bold text-[var(--pantone-darkest-hour)] shadow-md transition-transform hover:scale-105 active:scale-95",
  btnSecondary: "rounded-lg bg-[var(--pantone-exuberant-orange)] px-6 py-3 font-bold text-white shadow-md transition-transform hover:scale-105 active:scale-95",
} as const;
