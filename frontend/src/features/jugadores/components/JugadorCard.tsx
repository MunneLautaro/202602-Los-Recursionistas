import React from "react";
import { ArrowRight } from "lucide-react"
import type { JugadorResponseDTO } from "../types";
import { calcularEdad } from "../utils";
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
  const iniciales = jugador.nombre
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((parte) => parte[0])
    .join("")
    .toUpperCase()

  return (
    <button
      type="button"
      className={jugadorCardStyles.card}
      onClick={handleClick}
    >
      <div className={jugadorCardStyles.header}>
        <span className={jugadorCardStyles.avatar} aria-hidden="true">{iniciales}</span>
        <h3 className={jugadorCardStyles.name}>{jugador.nombre}</h3>
        <span className={jugadorCardStyles.position}>{jugador.posicion}</span>
      </div>
      <div className={jugadorCardStyles.body}>
        <p className={jugadorCardStyles.age}>
          Edad: {calcularEdad(jugador.fechaNacimiento) ?? "No disponible"}
        </p>
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
      <ArrowRight className={jugadorCardStyles.icon} aria-hidden="true" size={18} />
    </button>
  );
};
