const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

export interface ApiErrorResponse {
  message?: string;
  error?: string;
  status?: number;
}

export class ApiError extends Error {
  readonly status: number;
  readonly details?: unknown;

  constructor(message: string, status: number, details?: unknown) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.details = details;
  }
}

function hasHeader(headers: HeadersInit, name: string): boolean {
  if (headers instanceof Headers) {
    return headers.has(name);
  }

  if (Array.isArray(headers)) {
    return headers.some(([key]) => key.toLowerCase() === name.toLowerCase());
  }

  return Object.keys(headers).some(
    (key) => key.toLowerCase() === name.toLowerCase(),
  );
}

function getErrorMessage(data: unknown, status: number, statusText: string) {
  if (typeof data === "object" && data !== null) {
    const errorData = data as ApiErrorResponse;
    if (errorData.message || errorData.error) {
      return errorData.message ?? errorData.error ?? "Error de API";
    }
  }

  if (typeof data === "string" && data.trim()) {
    return data;
  }

  return `Error ${status}: ${statusText}`;
}

export async function fetchClient<T>(
  endpoint: string,
  options: RequestInit = {},
): Promise<T> {
  const url = endpoint.startsWith("http") ? endpoint : `${BASE_URL}${endpoint}`;
  const headers = new Headers(options.headers);
  if (!hasHeader(options.headers ?? {}, "Authorization")) {
    const token = localStorage.getItem("token");
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }
  }
  if (!hasHeader(options.headers ?? {}, "Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  const config: RequestInit = {
    ...options,
    headers,
  };

  const response = await fetch(url, config);

  const contentType = response.headers.get("content-type");
  const data: unknown =
    contentType && contentType.includes("application/json")
      ? await response.json()
      : await response.text();

  if (!response.ok) {
    if (response.status === 401) {
      window.dispatchEvent(new CustomEvent("auth:invalidated"));
    }
    throw new ApiError(
      getErrorMessage(data, response.status, response.statusText),
      response.status,
      data,
    );
  }

  return data as T;
}
