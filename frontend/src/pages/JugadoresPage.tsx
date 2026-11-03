import React, { useCallback, useEffect, useMemo, useState } from "react"
import { useSearchParams } from "react-router-dom"
import {
  fetchJugadores,
  fetchJugadorDetalle,
} from "../features/jugadores/api/jugadoresApi"
import type {
  PaginatedJugadores,
  JugadorFiltroRequestDTO,
  JugadorResponseDTO,
} from "../features/jugadores/types"
import { JugadorCard } from "../features/jugadores/components/JugadorCard"
import { JugadorDetailModal } from "../features/jugadores/components/JugadorDetailModal"
import { JugadoresFilters } from "../features/jugadores/components/JugadoresFilters"
import { PaginationControls } from "../features/jugadores/components/PaginationControls"

export const JugadoresPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams()
  const equipoIdParam = searchParams.get("equipoId")
  const equipoId = equipoIdParam && /^\d+$/.test(equipoIdParam) ? Number(equipoIdParam) : undefined
  const [jugadores, setJugadores] = useState<PaginatedJugadores | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [filters, setFilters] = useState<JugadorFiltroRequestDTO>(() => ({ equipoId }))
  const [page, setPage] = useState(0)
  const [size] = useState(12)

  const effectiveFilters = useMemo(
    () => ({ ...filters, equipoId }),
    [equipoId, filters],
  )

  const [selectedJugador, setSelectedJugador] =
    useState<JugadorResponseDTO | null>(null)
  const [modalOpen, setModalOpen] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState<string | null>(null)

  const loadJugadores = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await fetchJugadores(effectiveFilters, page, size)
      setJugadores(data)
    } catch (error) {
      setError(error instanceof Error ? error.message : "Error al cargar jugadores")
    } finally {
      setLoading(false)
    }
  }, [effectiveFilters, page, size])

  const openDetalle = async (id: number) => {
    setDetailLoading(true)
    setDetailError(null)
    try {
      const data = await fetchJugadorDetalle(id)
      setSelectedJugador(data)
      setModalOpen(true)
    } catch (error) {
      setDetailError(
        error instanceof Error ? error.message : "Error al cargar detalle",
      )
    } finally {
      setDetailLoading(false)
    }
  }

  useEffect(() => {
    const timer = window.setTimeout(() => {
      void loadJugadores()
    }, 0)
    return () => window.clearTimeout(timer)
  }, [loadJugadores])

  return (
    <div className="container mx-auto p-4">
      <h1
        className="text-2xl font-bold mb-4"
        style={{ color: "var(--text-h)" }}
      >
        Lista de Jugadores
      </h1>

      <JugadoresFilters filters={filters} setFilters={setFilters} />
      {equipoId !== undefined && (
        <div className="mb-3 flex items-center justify-between rounded border border-[var(--accent-border)] bg-[var(--accent-bg)] p-2">
          <span>Jugadores filtrados por equipo #{equipoId}</span>
          <button type="button" onClick={() => setSearchParams({})}>Limpiar filtro</button>
        </div>
      )}

      {loading && <p>Cargando jugadores...</p>}
      {error && <p className="text-red-500">{error}</p>}

      {!loading && jugadores && (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mt-4">
            {jugadores.content.map((j) => (
              <JugadorCard key={j.id} jugador={j} onClick={openDetalle} />
            ))}
          </div>
          <PaginationControls
            page={jugadores.number}
            totalPages={jugadores.totalPages}
            onPageChange={(p) => setPage(p)}
          />
        </>
      )}

      {modalOpen && selectedJugador && (
        <JugadorDetailModal
          jugador={selectedJugador}
          onClose={() => setModalOpen(false)}
        />
      )}
      {detailLoading && <p>Cargando detalle...</p>}
      {detailError && <p className="text-red-500">{detailError}</p>}
    </div>
  )
}
