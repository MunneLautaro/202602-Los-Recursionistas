import type { z } from "zod"

export interface EsquemaFormulario<T extends z.ZodType> {
  esquema: T
  valoresIniciales: z.input<T>
}
