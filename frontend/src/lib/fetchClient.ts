const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080"

export interface ApiErrorResponse {
  message?: string
  error?: string
  status?: number
}

export async function fetchClient<T>(
  endpoint: string,
  options: RequestInit = {},
): Promise<T> {
  const url = endpoint.startsWith("http") ? endpoint : `${BASE_URL}${endpoint}`

  const config: RequestInit = {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...options.headers,
    },
  }

  const response = await fetch(url, config)

  let data: any = null
  const contentType = response.headers.get("content-type")
  if (contentType && contentType.includes("application/json")) {
    data = await response.json()
  } else {
    data = await response.text()
  }

  if (!response.ok) {
    const errorMessage =
      typeof data === "object" && data !== null
        ? data.message || data.error || "Ocurrió un error en la solicitud"
        : data || `Error ${response.status}: ${response.statusText}`
    throw new Error(errorMessage)
  }

  return data as T
}
