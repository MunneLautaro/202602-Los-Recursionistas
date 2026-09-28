import React from "react";
import type { JugadorResponseDTO } from "../types";
import { jugadorCardStyles } from "./JugadorCard.styles";

export interface JugadorCardProps {
  jugador: JugadorResponseDTO;
  onClick: (id: number) => void;
}

export const JugadorCard: React.FC<JugadorCardProps> = ({
  jugador,
  onClick,
}) => {
  const handleClick = () => onClick(jugador.id);

  return (
    <div
      className={jugadorCardStyles.card}
      onClick={handleClick}
      role="button"
      tabIndex={0}
      onKeyPress={(e) => e.key === "Enter" && handleClick()}
    >
      <div className={jugadorCardStyles.header}>
        <h3 className={jugadorCardStyles.name}>{jugador.nombre}</h3>
        <span className={jugadorCardStyles.position}>{jugador.posicion}</span>
      </div>
      <div className={jugadorCardStyles.body}>
        <p className={jugadorCardStyles.age}>Edad: {jugador.edad}</p>
        {jugador.equipo && (
          <p className={jugadorCardStyles.team}>
            Equipo: {jugador.equipo.nombre}
          </p>
        )}
        {jugador.liga && (
          <p className={jugadorCardStyles.league}>
            Liga: {jugador.liga.nombre}
          </p>
        )}
      </div>
    </div>
  );
};
