import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { creditReloadFormStyles } from "./CreditReloadForm.styles";
import { creditReloadSchema, type CreditReloadValues } from "../schemas";

interface CreditReloadFormProps {
  disabled: boolean;
  onSubmit: (amount: number) => Promise<void>;
}

export function CreditReloadForm({ disabled, onSubmit }: CreditReloadFormProps) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<CreditReloadValues>({
    resolver: zodResolver(creditReloadSchema),
    defaultValues: { monto: 0 },
  });
  const submit = async ({ monto }: CreditReloadValues) => onSubmit(monto);

  return (
    <form className={creditReloadFormStyles.form} onSubmit={(event) => void handleSubmit(submit)(event)}>
      <label className={creditReloadFormStyles.label}>
        Recargar créditos ficticios
        <input
          className={creditReloadFormStyles.input}
          type="number"
          min="0.01"
          step="0.01"
          {...register("monto", { valueAsNumber: true })}
          disabled={disabled}
          required
          aria-invalid={Boolean(errors.monto)}
        />
      </label>
      {errors.monto && <p role="alert" className="text-[var(--secondary)]">{errors.monto.message}</p>}
      <button className={creditReloadFormStyles.button} type="submit" disabled={disabled || isSubmitting}>
        {disabled || isSubmitting ? "Procesando..." : "Recargar"}
      </button>
    </form>
  );
}
