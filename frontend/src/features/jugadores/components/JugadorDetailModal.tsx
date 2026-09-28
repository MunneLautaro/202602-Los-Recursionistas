import React from "react";
import type { JugadorResponseDTO } from "../types";
import { jugadorDetailModalStyles } from "./JugadorDetailModal.styles";

interface JugadorDetailModalProps {
  jugador: JugadorResponseDTO;
  onClose: () => void;
}

export const JugadorDetailModal: React.FC<JugadorDetailModalProps> = ({
  jugador,
  onClose,
}) => {
  return (
    <div className={jugadorDetailModalStyles.overlay} onClick={onClose}>
      <div
        className={jugadorDetailModalStyles.modal}
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
      >
        <button
          className={jugadorDetailModalStyles.closeButton}
          onClick={onClose}
        >
          ×
        </button>
        <h2 className={jugadorDetailModalStyles.title}>{jugador.nombre}</h2>
        <p className={jugadorDetailModalStyles.field}>
          Posición: {jugador.posicion}
        </p>
        <p className={jugadorDetailModalStyles.field}>Edad: {jugador.edad}</p>
        {jugador.equipo && (
          <p className={jugadorDetailModalStyles.field}>
            Equipo: {jugador.equipo.nombre}
          </p>
        )}
        {jugador.liga && (
          <p className={jugadorDetailModalStyles.field}>
            Liga: {jugador.liga.nombre}
          </p>
        )}
      </div>
    </div>
  );
};
