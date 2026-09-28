import { useState } from "react"
import type { ChangeEvent, FormEvent } from "react"
import { useNavigate } from "react-router-dom"
import { authApi } from "../api/authApi"
import type { RegisterFormData, RegisterFormErrors } from "../types"

export function useRegister() {
  const navigate = useNavigate()
  const [formData, setFormData] = useState<RegisterFormData>({
    nombreUsuario: "",
    contrasena: "",
    confirmarContrasena: "",
  })

  const [errors, setErrors] = useState<RegisterFormErrors>({})
  const [isLoading, setIsLoading] = useState(false)
  const [isSuccess, setIsSuccess] = useState(false)

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))

    if (errors[name as keyof RegisterFormErrors]) {
      setErrors((prev) => ({ ...prev, [name]: undefined }))
    }
  }

  const validate = (): boolean => {
    const newErrors: RegisterFormErrors = {}

    if (!formData.nombreUsuario.trim()) {
      newErrors.nombreUsuario = "El nombre de usuario es requerido."
    } else if (formData.nombreUsuario.length < 3) {
      newErrors.nombreUsuario =
        "El nombre de usuario debe tener al menos 3 caracteres."
    }

    if (!formData.contrasena) {
      newErrors.contrasena = "La contraseña es requerida."
    } else if (formData.contrasena.length < 4) {
      newErrors.contrasena = "La contraseña debe tener al menos 4 caracteres."
    }

    if (!formData.confirmarContrasena) {
      newErrors.confirmarContrasena = "Debes confirmar la contraseña."
    } else if (formData.contrasena !== formData.confirmarContrasena) {
      newErrors.confirmarContrasena = "Las contraseñas no coinciden."
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
    } catch (err: any) {
      setErrors({
        general:
          err.message ||
          "Error al registrar usuario. Es posible que el nombre de usuario ya exista.",
      })
    } finally {
      setIsLoading(false)
    }
  }

  return {
    formData,
    errors,
    isLoading,
    isSuccess,
    handleChange,
    handleSubmit,
  }
}
