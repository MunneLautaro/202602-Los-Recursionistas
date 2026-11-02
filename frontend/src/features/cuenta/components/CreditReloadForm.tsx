import { useState } from "react";
import type { FormEvent } from "react";
import { creditReloadFormStyles } from "./CreditReloadForm.styles";

interface CreditReloadFormProps {
  disabled: boolean;
  onSubmit: (amount: number) => Promise<void>;
}

export function CreditReloadForm({ disabled, onSubmit }: CreditReloadFormProps) {
  const [amount, setAmount] = useState("");
  const [validationError, setValidationError] = useState<string | null>(null);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const parsedAmount = Number(amount);
    if (!Number.isFinite(parsedAmount) || parsedAmount <= 0) {
      setValidationError("El monto debe ser un número positivo.");
      return;
    }

    setValidationError(null);
    await onSubmit(parsedAmount);
    setAmount("");
  };

  return (
    <form className={creditReloadFormStyles.form} onSubmit={handleSubmit}>
      <label className={creditReloadFormStyles.label}>
        Recargar créditos ficticios
        <input
          className={creditReloadFormStyles.input}
          type="number"
          min="0.01"
          step="0.01"
          value={amount}
          onChange={(event) => setAmount(event.target.value)}
          disabled={disabled}
          required
        />
      </label>
      {validationError && <p role="alert" className="text-[var(--secondary)]">{validationError}</p>}
      <button className={creditReloadFormStyles.button} type="submit" disabled={disabled}>
        {disabled ? "Procesando..." : "Recargar"}
      </button>
    </form>
  );
}
