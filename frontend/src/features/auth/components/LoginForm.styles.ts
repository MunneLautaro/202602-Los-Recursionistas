export const loginFormStyles = {
  container:
    "ui-card w-full max-w-md overflow-hidden p-8 sm:p-10 before:pointer-events-none before:absolute before:-top-24 before:-left-24 before:h-56 before:w-56 before:rounded-full before:bg-lavender/25 before:blur-3xl before:content-['']",
  header: "relative mb-8 text-left",
  title: "text-3xl font-semibold tracking-tight text-white sm:text-4xl",
  subtitle: "mt-3 text-sm leading-relaxed text-white/55",
  form: "relative flex flex-col gap-5",
  fieldGroup: "flex flex-col gap-2 text-left",
  label: "text-sm font-medium text-white/60",
  input: "ui-input",
  inputError:
    "border-coral/70 focus:border-coral! focus:shadow-[0_0_0_4px_rgba(255,127,122,0.14)]!",
  errorText: "mt-0.5 text-xs font-medium text-coral",
  alertError: "ui-alert-error mb-5 text-center",
  submitBtn: "ui-btn-primary mt-3 w-full py-3.5 text-base",
  footer: "relative mt-8 border-t border-white/[0.06] pt-6 text-center text-sm text-white/50",
  footerLink:
    "ml-1.5 cursor-pointer font-semibold text-mint transition-colors hover:text-white",
} as const
