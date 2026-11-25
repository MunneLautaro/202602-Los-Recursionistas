import {
  Bar,
  CartesianGrid,
  ComposedChart,
  Legend,
  Line,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import type { DiaCotizacion } from "../types";
import { graficoCotizacionStyles as styles } from "./GraficoCotizacion.styles";
import {
  COLOR_CURSOR,
  COLOR_GRILLA,
  COLOR_LAVANDA,
  COLOR_MINT,
  ESTILO_EJE,
  ESTILO_TOOLTIP,
  formatearDia,
  formatearVariacion,
} from "./temaGraficos";

export interface GraficoCotizacionProps {
  serie: DiaCotizacion[];
}

const NOMBRE_VARIACION = "Variación promedio";

function formatearValorTooltip(valor: unknown, nombre: unknown): string {
  if (nombre !== NOMBRE_VARIACION) {
    return String(valor);
  }
  const numero = valor === null || valor === undefined ? null : Number(valor);
  return formatearVariacion(numero);
}

export function GraficoCotizacion({ serie }: GraficoCotizacionProps) {
  const sinCotizaciones = serie.every((dia) => dia.jugadoresCotizados === 0);

  return (
    <figure className={styles.marco}>
      <figcaption className={styles.titulo}>Jugadores cotizados y variación promedio por día</figcaption>
      {sinCotizaciones ? (
        <p className={styles.vacio}>Sin recálculos en el período</p>
      ) : (
        <div className={styles.lienzo} role="img" aria-label="Gráfico de jugadores cotizados por día y variación promedio de cotización">
          <ResponsiveContainer width="100%" height="100%">
            <ComposedChart data={serie} margin={{ top: 4, right: 0, left: -12, bottom: 0 }}>
              <CartesianGrid stroke={COLOR_GRILLA} vertical={false} />
              <XAxis dataKey="dia" tickFormatter={formatearDia} tick={ESTILO_EJE} tickLine={false} axisLine={false} minTickGap={16} />
              <YAxis yAxisId="jugadores" allowDecimals={false} tick={ESTILO_EJE} tickLine={false} axisLine={false} />
              <YAxis
                yAxisId="variacion"
                orientation="right"
                tickFormatter={(valor: number) => formatearVariacion(valor)}
                tick={ESTILO_EJE}
                tickLine={false}
                axisLine={false}
              />
              <Tooltip
                contentStyle={ESTILO_TOOLTIP}
                cursor={{ fill: COLOR_CURSOR }}
                labelFormatter={(dia) => formatearDia(String(dia))}
                formatter={(valor, nombre) => formatearValorTooltip(valor, nombre)}
              />
              <Legend wrapperStyle={{ fontSize: 12 }} />
              <Bar yAxisId="jugadores" dataKey="jugadoresCotizados" name="Jugadores cotizados" fill={COLOR_LAVANDA} radius={[4, 4, 0, 0]} />
              <Line
                yAxisId="variacion"
                type="monotone"
                dataKey="variacionPromedio"
                name={NOMBRE_VARIACION}
                stroke={COLOR_MINT}
                strokeWidth={2}
                connectNulls={false}
                dot={{ r: 3 }}
              />
            </ComposedChart>
          </ResponsiveContainer>
        </div>
      )}
    </figure>
  );
}
