# Modelo de datos de interfaz

Este modelo describe entidades que ya llegan de los contratos actuales o que
se derivan para presentación. No crea endpoints ni cambia DTOs del backend.

## Entidades

### Jugador

- `id: number`
- `nombre: string`
- `posicion: string`
- `fechaNacimiento: string | null`
- `equipo?: { id: number; nombre: string }`
- `liga?: { id: number; nombre: string }`
- `imagenUrl?: string` solo si el contrato actual la entrega

Se muestra edad, posición, liga y equipo. Si no hay imagen, se generan iniciales
desde `nombre`; no se inventan URLs.

### Equipo

- `id: number`
- `nombre: string`
- `nombreCorto: string`
- `sigla: string`
- `escudoUrl: string`
- `liga: { id: number; nombre: string }` cuando esté disponible en el contrato

El escudo puede sustituirse por sigla o iniciales cuando falte la URL. La
selección del equipo produce un filtro removible en jugadores.

### RankingPlayer

- `jugadorId: number`
- `nombre: string`
- `posicion: string`
- `equipo: string | null`
- `liga: string | null`
- `valorActual: number`
- `valorAnterior: number | null`
- `variacion: number | null`

La variación se presenta con color, icono, signo y texto (`positiva`,
`negativa` o `neutra`) para no depender solo del color.

### Orden

- `id: number`
- `jugadorId: number`
- `tipo: "COMPRA" | "VENTA"`
- `cantidadOriginal: number`
- `cantidadDisponible: number`
- `precioUnitario: number`
- `precioTotalRemanente: number`
- `estado: string`
- `fechaCreacion: string`
- `jugadorNombre?: string`

La cantidad del formulario debe ser un entero positivo. La respuesta del
backend conserva autoridad sobre saldo, tokens y estado de la operación.

### Cuenta

- `usuarioId: number`
- `saldo: number`
- `saldoReservado: number`
- `saldoDisponible: number`
- `ordenes: Orden[]` cuando la vista las cargue
- `transacciones: Transaction[]` cuando la vista las cargue

La recarga requiere `monto` finito y positivo; el botón queda bloqueado durante
la solicitud y el saldo solo se actualiza tras respuesta exitosa.

### Formulario

- `valores`: tipo inferido del esquema Zod
- `erroresCampo`: mapa de nombre de campo a mensaje en español
- `errorGeneral`: mensaje de servidor o error no asociado a un campo
- `enviando: boolean`
- `valido: boolean`

Todos los formularios parten de valores iniciales explícitos, no permiten
doble envío y conservan sus valores cuando el servidor rechaza la operación.

## Entidades de interacción compartida

### EstadoConsulta

- `estado: "cargando" | "exito" | "vacio" | "error"`
- `mensaje?: string`
- `accion?: { etiqueta: string; ejecutar: () => void }`

Se usa en jugadores, equipos, ranking, mercado, cuenta y detalle de jugador.
`cargando` muestra skeleton; `vacio` muestra contexto y limpiar/volver cuando
aplique; `error` muestra reintentar cuando sea posible.

### FiltroActivo

- `clave: string`
- `etiqueta: string`
- `valor: string`
- `remover: () => void`

Los filtros explícitos conservan su semántica actual; solo el nombre de
jugadores se aplica automáticamente después de 400 ms de inactividad.

## Validaciones

- Fechas de cotización: fecha inicial no posterior a fecha final.
- Nombre, liga, equipo y posición: respetar tipos y opciones actuales.
- Orden: jugador requerido, tipo válido y cantidad entera positiva.
- Recarga: monto numérico finito y positivo.
- Login/registro: reglas actuales de los hooks y confirmación de contraseña.
- Errores de servidor: se muestran en español cuando el backend entrega
  mensaje; nunca se reemplazan por datos inventados ni se resetea el formulario.

