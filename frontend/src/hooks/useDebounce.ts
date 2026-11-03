import { useEffect, useState } from "react"
import type { UseDebounceOptions } from "../types"

export function useDebounce<T>(
  valor: T,
  { delayMs = 400 }: UseDebounceOptions = {},
): T {
  const [valorDebounced, setValorDebounced] = useState(valor)

  useEffect(() => {
    const timer = window.setTimeout(() => setValorDebounced(valor), delayMs)
    return () => window.clearTimeout(timer)
  }, [delayMs, valor])

  return valorDebounced
}
