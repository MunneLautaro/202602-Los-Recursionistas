export interface RegisterPayload {
  nombreUsuario: string
  contrasena: string
  saldo: number
  habilitado: boolean
}

export interface UsuarioResponse {
  id?: number
  nombreUsuario: string
  saldo: number
  habilitado: boolean
  fechaCreacion?: string
}

export interface RegisterFormData {
  nombreUsuario: string
  contrasena: string
  confirmarContrasena: string
}

export interface RegisterFormErrors {
  nombreUsuario?: string
  contrasena?: string
  confirmarContrasena?: string
  general?: string
}

export interface LoginPayload {
  nombreUsuario: string
  contrasena: string
}

export interface LoginResponse {
  token: string
}

export interface LoginFormData {
  nombreUsuario: string
  contrasena: string
}

export interface LoginFormErrors {
  nombreUsuario?: string
  contrasena?: string
  general?: string
}
