export const orderFormStyles = {
  form: "grid gap-3 rounded-lg border border-[var(--border)] p-4 text-left",
  input: "rounded border border-[var(--border)] bg-[var(--bg)] px-3 py-2",
  actions: "flex gap-2",
  button: "rounded bg-[var(--pantone-sun-glare)] px-4 py-2 font-semibold text-[var(--pantone-darkest-hour)] disabled:opacity-50",
  error: "text-[var(--secondary)]",
} as const;
