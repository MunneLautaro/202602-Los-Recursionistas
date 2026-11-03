export interface UseDebounceOptions {
  delayMs?: number
}

export type UseDebounce = <T>(
  valor: T,
  opciones?: UseDebounceOptions,
) => T
