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
import { EstadoConsulta } from "../components/EstadoConsulta"
import { X } from "lucide-react"

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
  const updateFilters: React.Dispatch<React.SetStateAction<JugadorFiltroRequestDTO>> =
    (update) => {
      setPage(0)
      setFilters(update)
    }

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
      if (data.totalPages > 0 && page >= data.totalPages) {
        setPage(data.totalPages - 1)
        return
      }
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

      <JugadoresFilters filters={filters} setFilters={updateFilters} />
      {equipoId !== undefined && (
        <div className="mb-3 flex flex-wrap items-center gap-2 rounded border border-[var(--accent-border)] bg-[var(--accent-bg)] p-2">
          <span className="text-sm">Filtro de equipo: #{equipoId}</span>
          <button
            type="button"
            className="inline-flex items-center gap-1 rounded px-2 py-1 text-sm font-semibold text-[var(--accent)] focus-visible:outline focus-visible:outline-2 focus-visible:outline-[var(--accent)]"
            onClick={() => {
              setSearchParams({})
              setPage(0)
            }}
          >
            Quitar <X size={14} aria-hidden="true" />
          </button>
        </div>
      )}

      {loading && <EstadoConsulta estado="cargando" mensaje="Buscando jugadores..." />}
      {!loading && error && (
        <EstadoConsulta
          estado="error"
          mensaje={error}
          accion={{ etiqueta: "Reintentar", ejecutar: () => void loadJugadores() }}
        />
      )}
      {!loading && !error && jugadores?.content.length === 0 && (
        <EstadoConsulta
          estado="vacio"
          mensaje="No encontramos jugadores con los filtros seleccionados."
          accion={{ etiqueta: "Limpiar filtros", ejecutar: () => updateFilters({}) }}
        />
      )}
      {!loading && !error && jugadores && jugadores.content.length > 0 && (
        <>
          <p className="mt-4 text-sm text-[var(--text)]">
            {jugadores.totalElements} jugadores encontrados
          </p>
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
      {detailLoading && <p aria-live="polite">Cargando detalle...</p>}
      {detailError && <p role="alert" className="text-[var(--secondary)]">{detailError}</p>}
    </div>
  )
}
