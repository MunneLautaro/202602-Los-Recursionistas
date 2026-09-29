import React, { useEffect, useState } from "react";
import {
  fetchJugadores,
  fetchJugadorDetalle,
} from "../features/jugadores/api/jugadoresApi";
import type {
  PaginatedJugadores,
  JugadorFiltroRequestDTO,
  JugadorResponseDTO,
} from "../features/jugadores/types";
import { JugadorCard } from "../features/jugadores/components/JugadorCard";
import { JugadorDetailModal } from "../features/jugadores/components/JugadorDetailModal";
import { JugadoresFilters } from "../features/jugadores/components/JugadoresFilters";
import { PaginationControls } from "../features/jugadores/components/PaginationControls";
import { Layout } from "../components";

export const JugadoresPage: React.FC = () => {
  const [jugadores, setJugadores] = useState<PaginatedJugadores | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFilters] = useState<JugadorFiltroRequestDTO>({});
  const [page, setPage] = useState(0);
  const [size] = useState(12);

  const [selectedJugador, setSelectedJugador] =
    useState<JugadorResponseDTO | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState<string | null>(null);

  const loadJugadores = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchJugadores(filters, page, size);
      setJugadores(data);
    } catch (e: any) {
      setError(e.message ?? "Error al cargar jugadores");
    } finally {
      setLoading(false);
    }
  };

  const openDetalle = async (id: number) => {
    setDetailLoading(true);
    setDetailError(null);
    try {
      const data = await fetchJugadorDetalle(id);
      setSelectedJugador(data);
      setModalOpen(true);
    } catch (e: any) {
      setDetailError(e.message ?? "Error al cargar detalle");
    } finally {
      setDetailLoading(false);
    }
  };

  useEffect(() => {
    loadJugadores();
  }, [filters, page]);

  return (
    <Layout>
      <div className="container mx-auto p-4">
        <h1
          className="text-2xl font-bold mb-4"
          style={{ color: "var(--text-h)" }}
        >
          Lista de Jugadores
        </h1>

        <JugadoresFilters filters={filters} setFilters={setFilters} />

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
    </Layout>
  );
};
