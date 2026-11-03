import { z } from "zod"

export const creditReloadSchema = z.object({
  monto: z.number({ message: "Ingresá un monto." }).finite().positive(
    "El monto debe ser positivo.",
  ),
})

export type CreditReloadValues = z.infer<typeof creditReloadSchema>
export const creditReloadValoresIniciales: CreditReloadValues = { monto: 0 }
