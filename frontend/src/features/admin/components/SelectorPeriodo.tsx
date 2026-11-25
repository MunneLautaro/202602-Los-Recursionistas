import type { PeriodoObservabilidad } from "../types";
import { selectorPeriodoStyles as styles } from "./SelectorPeriodo.styles";

const PERIODOS: PeriodoObservabilidad[] = [7, 30, 90];

export interface SelectorPeriodoProps {
  valor: PeriodoObservabilidad;
  onChange: (periodo: PeriodoObservabilidad) => void;
}

export function SelectorPeriodo({ valor, onChange }: SelectorPeriodoProps) {
  return (
    <div className={styles.grupo} role="group" aria-label="Período de las series históricas">
      {PERIODOS.map((periodo) => (
        <button
          key={periodo}
          type="button"
          className={periodo === valor ? styles.botonActivo : styles.boton}
          aria-pressed={periodo === valor}
          onClick={() => onChange(periodo)}
        >
          {periodo} días
        </button>
      ))}
    </div>
  );
}
