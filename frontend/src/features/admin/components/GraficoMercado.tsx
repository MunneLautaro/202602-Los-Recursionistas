import {
  Bar,
  BarChart,
  CartesianGrid,
  ComposedChart,
  Legend,
  Line,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import type { DiaMercado } from "../types";
import { graficoMercadoStyles as styles } from "./GraficoMercado.styles";
import {
  COLOR_AMBAR,
  COLOR_CORAL,
  COLOR_CURSOR,
  COLOR_GRILLA,
  COLOR_LAVANDA,
  COLOR_MINT,
  ESTILO_EJE,
  ESTILO_TOOLTIP,
  formatearCompacto,
  formatearCreditos,
  formatearDia,
} from "./temaGraficos";

export interface GraficoMercadoProps {
  serie: DiaMercado[];
}

export function GraficoMercado({ serie }: GraficoMercadoProps) {
  const sinActividad = serie.every(
    (dia) => dia.ordenesCompra + dia.ordenesVenta + dia.emparejamientos === 0,
  );

  return (
    <div className={styles.grilla}>
      <figure className={styles.marco}>
        <figcaption className={styles.titulo}>Órdenes creadas por día</figcaption>
        {sinActividad ? (
          <p className={styles.vacio}>Sin actividad en el período</p>
        ) : (
          <div className={styles.lienzo} role="img" aria-label="Gráfico de barras de órdenes de compra y venta creadas por día">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={serie} margin={{ top: 4, right: 8, left: -12, bottom: 0 }}>
                <CartesianGrid stroke={COLOR_GRILLA} vertical={false} />
                <XAxis dataKey="dia" tickFormatter={formatearDia} tick={ESTILO_EJE} tickLine={false} axisLine={false} minTickGap={16} />
                <YAxis allowDecimals={false} tick={ESTILO_EJE} tickLine={false} axisLine={false} />
                <Tooltip
                  contentStyle={ESTILO_TOOLTIP}
                  cursor={{ fill: COLOR_CURSOR }}
                  labelFormatter={(dia) => formatearDia(String(dia))}
                />
                <Legend wrapperStyle={{ fontSize: 12 }} />
                <Bar dataKey="ordenesCompra" name="Compras" stackId="ordenes" fill={COLOR_MINT} radius={[0, 0, 0, 0]} />
                <Bar dataKey="ordenesVenta" name="Ventas" stackId="ordenes" fill={COLOR_CORAL} radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}
      </figure>

      <figure className={styles.marco}>
        <figcaption className={styles.titulo}>Emparejamientos y volumen operado</figcaption>
        {sinActividad ? (
          <p className={styles.vacio}>Sin actividad en el período</p>
        ) : (
          <div className={styles.lienzo} role="img" aria-label="Gráfico de emparejamientos por día y volumen operado en créditos">
            <ResponsiveContainer width="100%" height="100%">
              <ComposedChart data={serie} margin={{ top: 4, right: 0, left: -12, bottom: 0 }}>
                <CartesianGrid stroke={COLOR_GRILLA} vertical={false} />
                <XAxis dataKey="dia" tickFormatter={formatearDia} tick={ESTILO_EJE} tickLine={false} axisLine={false} minTickGap={16} />
                <YAxis yAxisId="cantidad" allowDecimals={false} tick={ESTILO_EJE} tickLine={false} axisLine={false} />
                <YAxis
                  yAxisId="volumen"
                  orientation="right"
                  tickFormatter={formatearCompacto}
                  tick={ESTILO_EJE}
                  tickLine={false}
                  axisLine={false}
                />
                <Tooltip
                  contentStyle={ESTILO_TOOLTIP}
                  cursor={{ fill: COLOR_CURSOR }}
                  labelFormatter={(dia) => formatearDia(String(dia))}
                  formatter={(valor, nombre) =>
                    nombre === "Volumen" ? formatearCreditos(Number(valor)) : String(valor)
                  }
                />
                <Legend wrapperStyle={{ fontSize: 12 }} />
                <Bar yAxisId="cantidad" dataKey="emparejamientos" name="Emparejamientos" fill={COLOR_LAVANDA} radius={[4, 4, 0, 0]} />
                <Line
                  yAxisId="volumen"
                  type="monotone"
                  dataKey="volumenCreditos"
                  name="Volumen"
                  stroke={COLOR_AMBAR}
                  strokeWidth={2}
                  dot={false}
                />
              </ComposedChart>
            </ResponsiveContainer>
          </div>
        )}
      </figure>
    </div>
  );
}
