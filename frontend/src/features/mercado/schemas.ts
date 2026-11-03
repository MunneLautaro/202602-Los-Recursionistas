import { z } from "zod"

export const orderSchema = z.object({
  jugadorId: z.number({ message: "Seleccioná un jugador." }).int().positive(),
  cantidad: z.number({ message: "Ingresá una cantidad." }).int().positive(
    "La cantidad debe ser un entero positivo.",
  ),
})

export const orderFiltersSchema = z.object({
  jugadorNombre: z.string().optional(),
  tipo: z.enum(["COMPRA", "VENTA"]).optional(),
})

export type OrderFormValues = z.infer<typeof orderSchema>
export const orderValoresIniciales: OrderFormValues = {
  jugadorId: 0,
  cantidad: 0,
}
