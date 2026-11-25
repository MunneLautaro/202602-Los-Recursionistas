import { tarjetaMetricaStyles as styles } from "./TarjetaMetrica.styles";

export interface TarjetaMetricaProps {
  etiqueta: string;
  valor: string;
  nota?: string;
  tono?: "neutro" | "positivo" | "alerta";
}

const estiloPorTono = {
  neutro: styles.valorNeutro,
  positivo: styles.valorPositivo,
  alerta: styles.valorAlerta,
} as const;

export function TarjetaMetrica({ etiqueta, valor, nota, tono = "neutro" }: TarjetaMetricaProps) {
  return (
    <div className={styles.tile}>
      <span className={styles.etiqueta}>{etiqueta}</span>
      <span className={estiloPorTono[tono]} title={valor}>
        {valor}
      </span>
      {nota ? <span className={styles.nota}>{nota}</span> : null}
    </div>
  );
}
