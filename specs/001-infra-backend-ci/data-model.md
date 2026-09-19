# Data Model: Infraestructura base del backend

## Objetivo

Este documento define el modelo minimo de infraestructura que soporta la base del backend y la validacion automatica del proyecto. El enfoque es estructural y de plataforma, no de negocio de dominio.

## Entidades de infraestructura

### ConfiguracionAplicacion

**Descripcion**: representa la configuracion global de la aplicacion y los valores base necesarios para ejecutar el servicio.

**Campos**:

- `clave`: identificador unico de la configuracion
- `valor`: valor asociado para la propiedad
- `descripcion`: detalle de uso y alcance
- `activo`: indicador de estado habilitado

**Reglas**:

- `clave` debe ser unica
- `valor` no debe quedar vacio para propiedades obligatorias
- el sistema debe mantener una sola fuente de verdad en `application.properties`

**Relaciones**:

- es usada por la configuracion central de Spring Boot y por futuros servicios de dominio

### ExcepcionApi

**Descripcion**: modelo base para errores del backend que se traducen a respuestas HTTP consistentes.

**Campos**:

- `codigo`: identificador de error o estado
- `mensaje`: descripcion legible para clientes y logs
- `detalle`: contexto adicional para diagnostico
- `timestamp`: marca temporal del evento

**Reglas**:

- debe ser no chequeada (runtime)
- debe tener traduccion a respuesta HTTP estable
- el `@ControllerAdvice` debe mapearla a un payload consistente

**Relaciones**:

- se genera desde servicios, validaciones y excepciones de dominio

### ResultadoValidacion

**Descripcion**: representa el resultado de una validacion de infraestructura o pipeline en la fase inicial.

**Campos**:

- `nombre`: nombre de la validacion
- `estado`: exitoso o fallido
- `mensaje`: detalle del resultado
- `contexto`: informacion de ejecucion relevante

**Reglas**:

- cada validacion debe producir un estado determinista
- los fallos deben quedar visibles para el equipo
- la ejecucion de CI debe usar este concepto como puerta de acceso para aceptar cambios

**Relaciones**:

- se usa en el workflow de GitHub Actions y en pruebas de modelo y servicio

## Relaciones de arquitectura

- `controller` usa `service` para orquestar la ejecucion
- `service` usa `persistence` y `model` sin violar el acoplamiento entre capas
- `exceptions` actua como puente para errores de negocio y de transporte
- `configuration` centraliza beans, OpenAPI y otros ajustes globales
- `security` queda preparada para autenticacion y autorizacion futuras

## Validaciones y restricciones

- toda clase nueva debe formar parte de los paquetes previstos por la constitution
- los nombres de entidades y atributos deben usar ASCII y formato en espanol tecnico
- los modelos deben proteger invariantes basicos y no mezclar logica de presentacion
- la persistencia debe mantenerse separada de la capa de servicio
