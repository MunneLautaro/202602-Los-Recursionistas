delete from jugadores;
delete from ligas;

insert into ligas (id, nombre, codigo, activa)
values (1, 'Premier League', 'PREMIER', true);

insert into jugadores (id, nombre, equipo, posicion, liga_id, activo, disponible)
values (1, 'Jugador Demo', 'Equipo Demo', 'DELANTERO', 1, true, true);
