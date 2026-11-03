import { z } from "zod"

export const loginSchema = z.object({
  nombreUsuario: z.string().trim().min(1, "El nombre de usuario es obligatorio."),
  contrasena: z.string().min(1, "La contraseña es obligatoria."),
})

export const loginValoresIniciales: LoginFormValues = {
  nombreUsuario: "",
  contrasena: "",
}

export const registerSchema = z
  .object({
    nombreUsuario: z.string().trim().min(3, "Usá al menos 3 caracteres."),
    contrasena: z.string().min(6, "Usá al menos 6 caracteres."),
    confirmarContrasena: z.string().min(1, "Confirmá la contraseña."),
  })
  .refine((data) => data.contrasena === data.confirmarContrasena, {
    path: ["confirmarContrasena"],
    message: "Las contraseñas no coinciden.",
  })

export type LoginFormValues = z.infer<typeof loginSchema>
export type RegisterFormValues = z.infer<typeof registerSchema>

export const registerValoresIniciales: RegisterFormValues = {
  nombreUsuario: "",
  contrasena: "",
  confirmarContrasena: "",
}
