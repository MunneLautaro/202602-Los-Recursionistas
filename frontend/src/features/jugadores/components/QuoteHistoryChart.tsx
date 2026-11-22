import React, { useMemo } from "react"
import {
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
  type TooltipContentProps,
  type TooltipValueType,
} from "recharts"
import { Minus, TrendingDown, TrendingUp } from "lucide-react"
import type { QuotePoint } from "../types"
import { quoteHistoryChartStyles as styles } from "./QuoteHistoryChart.styles"

interface QuoteHistoryChartProps {
  points: QuotePoint[]
  jugadorNombre: string
}

interface ChartDatum {
  ts: number
  valor: number
  estrategiaUsada: string
}

const LINE_COLOR = "#5cffd2"
const SURFACE_COLOR = "#0c0e15"
const GRID_COLOR = "rgba(255, 255, 255, 0.06)"
const AXIS_TEXT_COLOR = "rgba(235, 238, 248, 0.45)"
const CURSOR_COLOR = "rgba(255, 255, 255, 0.22)"

const MAX_POINTS_WITH_DOTS = 30
const DAY_MS = 24 * 60 * 60 * 1000

const valueFormatter = new Intl.NumberFormat("es-AR", {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})
const axisValueFormatter = new Intl.NumberFormat("es-AR", {
  maximumFractionDigits: 2,
})
const compactAxisValueFormatter = new Intl.NumberFormat("es-AR", {
  notation: "compact",
  maximumFractionDigits: 1,
})
const percentFormatter = new Intl.NumberFormat("es-AR", {
  style: "percent",
  minimumFractionDigits: 1,
  maximumFractionDigits: 1,
  signDisplay: "exceptZero",
})
const shortDateFormatter = new Intl.DateTimeFormat("es-AR", {
  day: "2-digit",
  month: "short",
})
const shortDateWithYearFormatter = new Intl.DateTimeFormat("es-AR", {
  day: "2-digit",
  month: "short",
  year: "2-digit",
})
const fullDateFormatter = new Intl.DateTimeFormat("es-AR", {
  dateStyle: "medium",
  timeStyle: "short",
})

/** Redondea un paso al "número lindo" más cercano: 1, 2, 2.5, 5 o 10 × 10^k. */
function niceStep(rawStep: number): number {
  const exponent = Math.floor(Math.log10(rawStep))
  const magnitude = 10 ** exponent
  const fraction = rawStep / magnitude
  const niceFraction =
    [1, 2, 2.5, 5, 10].find((candidate) => fraction <= candidate) ?? 10
  return niceFraction * magnitude
}

/**
 * Eje Y con valores redondos y un margen arriba y abajo. No arranca en 0 para
 * que las variaciones chicas (p. ej. 950 → 1000) se vean y no queden planas.
 */
function computeYAxis(values: number[]): {
  domain: [number, number]
  ticks: number[]
} {
  const min = Math.min(...values)
  const max = Math.max(...values)
  const span = max - min || Math.max(Math.abs(max) * 0.1, 1)
  const step = niceStep(span / 4)
  let low = Math.floor(min / step) * step
  let high = Math.ceil(max / step) * step
  if (min - low < step * 0.25) low -= step
  if (high - max < step * 0.25) high += step
  if (low < 0 && min >= 0) low = 0
  const ticks: number[] = []
  for (let tick = low; tick <= high + step / 2; tick += step) {
    ticks.push(Number(tick.toFixed(10)))
  }
  return { domain: [low, high], ticks }
}

const prefersReducedMotion = (): boolean =>
  typeof window !== "undefined" &&
  typeof window.matchMedia === "function" &&
  window.matchMedia("(prefers-reduced-motion: reduce)").matches

/** Con un solo punto se abre el dominio X un día a cada lado para que no colapse. */
function computeXDomain(timestamps: number[]): [number, number] {
  const min = Math.min(...timestamps)
  const max = Math.max(...timestamps)
  return min === max ? [min - DAY_MS, max + DAY_MS] : [min, max]
}

function QuoteTooltip({
  active,
  payload,
}: TooltipContentProps<
  TooltipValueType,
  string | number
>): React.ReactElement | null {
  const datum = payload?.[0]?.payload as ChartDatum | undefined
  if (!active || !datum) return null
  return (
    <div className={styles.tooltip}>
      <p className={styles.tooltipDate}>{fullDateFormatter.format(datum.ts)}</p>
      <p className={styles.tooltipValue}>
        {valueFormatter.format(datum.valor)}
      </p>
      <p className={styles.tooltipMeta}>Estrategia: {datum.estrategiaUsada}</p>
    </div>
  )
}

const Trend: React.FC<{ change: number; ratio: number | null }> = ({
  change,
  ratio,
}) => {
  const label =
    ratio === null
      ? valueFormatter.format(change)
      : percentFormatter.format(ratio)
  if (change > 0) {
    return (
      <span className={styles.trendUp}>
        <TrendingUp size={16} aria-hidden="true" />
        <span>
          <span className="sr-only">Subió </span>
          {label}
        </span>
      </span>
    )
  }
  if (change < 0) {
    return (
      <span className={styles.trendDown}>
        <TrendingDown size={16} aria-hidden="true" />
        <span>
          <span className="sr-only">Bajó </span>
          {label}
        </span>
      </span>
    )
  }
  return (
    <span className={styles.trendFlat}>
      <Minus size={16} aria-hidden="true" />
      <span>Sin cambios</span>
    </span>
  )
}

export const QuoteHistoryChart: React.FC<QuoteHistoryChartProps> = ({
  points,
  jugadorNombre,
}) => {
  const data = useMemo<ChartDatum[]>(
    () =>
      points
        .map((point) => ({
          ts: new Date(point.fecha).getTime(),
          valor: Number(point.valor),
          estrategiaUsada: point.estrategiaUsada,
        }))
        .filter(
          (datum) => Number.isFinite(datum.ts) && Number.isFinite(datum.valor),
        ),
    [points],
  )

  if (data.length === 0) return null

  const values = data.map((datum) => datum.valor)
  const timestamps = data.map((datum) => datum.ts)
  const first = data[0]
  const last = data[data.length - 1]
  const change = last.valor - first.valor
  const ratio = first.valor !== 0 ? change / first.valor : null
  const min = Math.min(...values)
  const max = Math.max(...values)
  const xDomain = computeXDomain(timestamps)
  const spansYears =
    new Date(xDomain[0]).getFullYear() !== new Date(xDomain[1]).getFullYear()
  const tickDateFormatter = spansYears
    ? shortDateWithYearFormatter
    : shortDateFormatter
  const showDots = data.length <= MAX_POINTS_WITH_DOTS
  const yAxis = computeYAxis(values)
  const yTickFormatter =
    max >= 100_000 ? compactAxisValueFormatter : axisValueFormatter

  const chartLabel =
    `Evolución de la cotización de ${jugadorNombre}: ${data.length} ` +
    `${data.length === 1 ? "registro" : "registros"} entre ` +
    `${fullDateFormatter.format(first.ts)} y ${fullDateFormatter.format(last.ts)}, ` +
    `de ${valueFormatter.format(first.valor)} a ${valueFormatter.format(last.valor)}.`

  return (
    <div className={styles.root}>
      <dl className={styles.summary}>
        <div className={styles.tile}>
          <dt className={styles.tileLabel}>Último valor</dt>
          <dd className={styles.tileValue}>
            {valueFormatter.format(last.valor)}
          </dd>
        </div>
        <div className={styles.tile}>
          <dt className={styles.tileLabel}>Variación</dt>
          <dd>
            {data.length > 1 ? (
              <Trend change={change} ratio={ratio} />
            ) : (
              <span className={styles.trendFlat}>N/D</span>
            )}
          </dd>
        </div>
        <div className={styles.tile}>
          <dt className={styles.tileLabel}>Mínimo</dt>
          <dd className={styles.tileValue}>{valueFormatter.format(min)}</dd>
        </div>
        <div className={styles.tile}>
          <dt className={styles.tileLabel}>Máximo</dt>
          <dd className={styles.tileValue}>{valueFormatter.format(max)}</dd>
        </div>
      </dl>

      <figure className={styles.chartFrame} aria-label={chartLabel}>
        <ResponsiveContainer width="100%" height="100%">
          <LineChart
            data={data}
            margin={{ top: 8, right: 20, bottom: 4, left: 4 }}
          >
            <CartesianGrid vertical={false} stroke={GRID_COLOR} />
            <XAxis
              dataKey="ts"
              type="number"
              scale="time"
              domain={xDomain}
              tickFormatter={(ts: number) => tickDateFormatter.format(ts)}
              tick={{ fill: AXIS_TEXT_COLOR, fontSize: 11 }}
              tickLine={false}
              axisLine={{ stroke: GRID_COLOR }}
              minTickGap={24}
              tickMargin={8}
              padding={{ left: 12, right: 12 }}
            />
            <YAxis
              dataKey="valor"
              domain={yAxis.domain}
              ticks={yAxis.ticks}
              allowDataOverflow
              tickFormatter={(value: number) => yTickFormatter.format(value)}
              tick={{ fill: AXIS_TEXT_COLOR, fontSize: 11 }}
              tickLine={false}
              axisLine={false}
              width={56}
            />
            <Tooltip
              content={QuoteTooltip}
              cursor={{
                stroke: CURSOR_COLOR,
                strokeWidth: 1,
                strokeDasharray: "4 4",
              }}
              isAnimationActive={false}
            />
            <Line
              type="monotone"
              dataKey="valor"
              name="Cotización"
              stroke={LINE_COLOR}
              strokeWidth={2}
              dot={
                showDots
                  ? {
                      r: 4,
                      fill: LINE_COLOR,
                      stroke: SURFACE_COLOR,
                      strokeWidth: 2,
                    }
                  : false
              }
              activeDot={{
                r: 5,
                fill: LINE_COLOR,
                stroke: SURFACE_COLOR,
                strokeWidth: 2,
              }}
              isAnimationActive={!prefersReducedMotion() && data.length <= 200}
              animationDuration={600}
            />
          </LineChart>
        </ResponsiveContainer>
      </figure>

      {points.length >= 500 && (
        <p className={styles.note}>
          Se muestran las 500 cotizaciones más recientes del rango. Acotá las
          fechas para ver períodos anteriores.
        </p>
      )}
    </div>
  )
}

export default QuoteHistoryChart
