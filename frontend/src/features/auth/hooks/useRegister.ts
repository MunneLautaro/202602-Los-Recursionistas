import { useState } from "react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { useNavigate } from "react-router-dom"
import { authApi } from "../api/authApi"
import type { RegisterFormData } from "../types"
import { registerSchema, registerValoresIniciales } from "../schemas"

export function useRegister() {
  const navigate = useNavigate()
  const [isLoading, setIsLoading] = useState(false)
  const [isSuccess, setIsSuccess] = useState(false)
  const [generalError, setGeneralError] = useState<string>()
  const form = useForm<RegisterFormData>({ resolver: zodResolver(registerSchema), defaultValues: registerValoresIniciales })
  const handleSubmit = form.handleSubmit(async (formData) => {
    setIsLoading(true)
    setGeneralError(undefined)

    try {
      await authApi.registrar({
        nombreUsuario: formData.nombreUsuario.trim(),
        contrasena: formData.contrasena,
        saldo: 0.0,
        habilitado: true,
      })

      setIsSuccess(true)
      setTimeout(() => {
        navigate("/login")
      }, 2000)
    } catch (error) {
      setGeneralError(error instanceof Error ? error.message : "Error al registrar usuario. Es posible que el nombre de usuario ya exista.")
    } finally {
      setIsLoading(false)
    }
  })

  return {
    register: form.register,
    errors: form.formState.errors,
    generalError,
    isLoading,
    isSuccess,
    handleSubmit,
  }
}
