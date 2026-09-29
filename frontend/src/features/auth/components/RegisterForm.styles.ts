export const registerFormStyles = {
  container:
    "w-full max-w-md p-8 rounded-2xl bg-[var(--bg)] border border-[var(--border)] shadow-xl transition-all",
  header: "mb-6 text-center",
  title: "text-3xl font-extrabold text-[var(--text-h)] tracking-tight",
  subtitle: "mt-2 text-sm text-[var(--text)] opacity-80",
  form: "flex flex-col gap-5",
  fieldGroup: "flex flex-col gap-1.5 text-left",
  label: "text-sm font-semibold text-[var(--text)]",
  input:
    "w-full px-4 py-2.5 rounded-lg bg-[var(--bg)] border border-[var(--border)] text-[var(--text)] outline-none transition-all focus:border-[var(--pantone-blue-violet)] focus:ring-2 focus:ring-[var(--pantone-blue-violet)]/30",
  inputError:
    "border-[var(--pantone-exuberant-orange)] focus:border-[var(--pantone-exuberant-orange)] focus:ring-[var(--pantone-exuberant-orange)]/30",
  errorText: "text-xs font-medium text-[var(--pantone-exuberant-orange)] mt-1",
  alertError:
    "p-3 rounded-lg bg-[var(--pantone-exuberant-orange)]/10 border border-[var(--pantone-exuberant-orange)]/40 text-[var(--pantone-exuberant-orange)] text-sm text-center font-medium",
  alertSuccess:
    "p-3 rounded-lg bg-emerald-500/10 border border-emerald-500/40 text-emerald-600 dark:text-emerald-400 text-sm text-center font-medium",
  submitBtn:
    "w-full mt-2 py-3 px-6 rounded-xl bg-[var(--pantone-sun-glare)] text-[var(--pantone-darkest-hour)] font-bold text-base shadow-md transition-all hover:brightness-105 active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer",
  footer: "mt-6 text-center text-sm text-[var(--text)] opacity-90",
  footerLink:
    "font-bold text-[var(--pantone-blue-violet)] hover:underline ml-1 cursor-pointer",
} as const
