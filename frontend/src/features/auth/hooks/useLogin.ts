import { useState } from "react"
import type { ChangeEvent, FormEvent } from "react"
import { useNavigate } from "react-router-dom"
import { authApi } from "../api/authApi"
import type { LoginResponse } from "../types"
import type { LoginFormData, LoginFormErrors } from "../types"

export function useLogin() {
  const navigate = useNavigate()
  const [formData, setFormData] = useState<LoginFormData>({
    nombreUsuario: "",
    contrasena: "",
  })

  const [errors, setErrors] = useState<LoginFormErrors>({})
  const [isLoading, setIsLoading] = useState(false)

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))

    if (errors[name as keyof LoginFormErrors]) {
      setErrors((prev) => ({ ...prev, [name]: undefined }))
    }
  }

  const validate = (): boolean => {
    const newErrors: LoginFormErrors = {}

    if (!formData.nombreUsuario.trim()) {
      newErrors.nombreUsuario = "El nombre de usuario es requerido."
    }

    if (!formData.contrasena) {
      newErrors.contrasena = "La contraseña es requerida."
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()

    if (!validate()) {
      return
    }

    setIsLoading(true)
    setErrors({})

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
      setErrors({
        general:
          (error instanceof Error && error.message) ||
          "Credenciales inválidas. Verificá tu usuario y contraseña.",
      })
    } finally {
      setIsLoading(false)
    }
  }

  return {
    formData,
    errors,
    isLoading,
    handleChange,
    handleSubmit,
  }
}
