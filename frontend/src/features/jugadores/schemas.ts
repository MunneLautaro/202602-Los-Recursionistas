import { z } from "zod"

const isoDateSchema = z
  .string()
  .refine((value) => {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return false
    const date = new Date(`${value}T00:00:00Z`)
    const [year, month, day] = value.split("-").map(Number)
    return (
      !Number.isNaN(date.getTime()) &&
      date.getUTCFullYear() === year &&
      date.getUTCMonth() + 1 === month &&
      date.getUTCDate() === day
    )
  }, "Ingresa una fecha válida.")

export const quoteFiltersSchema = z
  .object({
    desde: isoDateSchema.optional().or(z.literal("")),
    hasta: isoDateSchema.optional().or(z.literal("")),
  })
  .superRefine((data, context) => {
    if (data.desde && data.hasta && data.desde > data.hasta) {
      context.addIssue({
        code: "custom",
        path: ["hasta"],
        message: "La fecha inicial no puede ser posterior a la fecha final.",
      })
    }
  })

export type QuoteFiltersValues = z.infer<typeof quoteFiltersSchema>
export const quoteFiltersValoresIniciales: QuoteFiltersValues = {}
