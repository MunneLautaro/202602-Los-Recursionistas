import { useState } from "react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { useNavigate } from "react-router-dom"
import { authApi } from "../api/authApi"
import type { LoginResponse } from "../types"
import type { LoginFormData } from "../types"
import { loginSchema, loginValoresIniciales } from "../schemas"

export function useLogin() {
  const navigate = useNavigate()
  const [isLoading, setIsLoading] = useState(false)
  const [generalError, setGeneralError] = useState<string>()
  const form = useForm<LoginFormData>({ resolver: zodResolver(loginSchema), defaultValues: loginValoresIniciales })
  const handleSubmit = form.handleSubmit(async (formData) => {
    setIsLoading(true)
    setGeneralError(undefined)

    try {
      const response = await authApi.login({
        nombreUsuario: formData.nombreUsuario.trim(),
        contrasena: formData.contrasena,
      })

      const loginResponse = response as LoginResponse
      localStorage.setItem("token", loginResponse.token)
      if (loginResponse.usuario?.id !== undefined) {
        localStorage.setItem("nombreUsuario", loginResponse.usuario.nombreUsuario)
        localStorage.setItem("userId", String(loginResponse.usuario.id))
        localStorage.setItem("usuario", JSON.stringify(loginResponse.usuario))
      } else {
        localStorage.setItem("nombreUsuario", formData.nombreUsuario.trim())
      }

      navigate("/")
      window.location.reload()
    } catch (error) {
      setGeneralError(error instanceof Error ? error.message : "Credenciales inválidas. Verificá tu usuario y contraseña.")
    } finally {
      setIsLoading(false)
    }
  })

  return {
    register: form.register,
    errors: form.formState.errors,
    generalError,
    isLoading,
    handleSubmit,
  }
}
