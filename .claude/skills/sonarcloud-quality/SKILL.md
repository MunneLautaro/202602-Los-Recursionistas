---
name: sonarcloud-quality
description: "Activa este skill al escribir, modificar o refactorizar código de backend en Java/Spring Boot, frontend en TypeScript/React o tests, para aplicar Clean Code y mantener la calidad de SonarCloud."
---

# SonarCloud Quality

## Objetivo

Aplicar reglas de Clean Code alineadas con SonarCloud y evitar la introducción de
code smells, vulnerabilidades o regresiones en el Quality Gate.

## Backend: Java 21 / Spring Boot

- Las clases de test y los métodos anotados con `@Test` MUST tener visibilidad
  package-private; nunca deben declararse `public`.
- Está prohibido usar `System.out.println` o `e.printStackTrace()`. Usar SLF4J:
  `log.error("...", e)` para excepciones y logging parametrizado con `{}`.
- No capturar `Exception` o `Throwable` de forma genérica, salvo en un handler
  global. Capturar siempre excepciones específicas.
- Evitar `@Autowired` en campos. Usar inyección por constructor, preferentemente
  mediante `@RequiredArgsConstructor` de Lombok.
- Extraer los literales `String` repetidos más de dos veces a constantes
  `private static final`.
- No usar `Optional.get()` directamente. Preferir `orElseThrow()`, `map()` o
  `ifPresent()`, según el caso.
- Respetar `camelCase` para métodos y variables, `UPPER_SNAKE_CASE` para
  constantes y nombres de tests compatibles con las expresiones regulares
  estándar de SonarCloud.

## Frontend: React 19 / TypeScript

- No usar `any` explícito. Definir correctamente interfaces, tipos y props.
- Usar siempre comparación estricta (`===` y `!==`).
- Mantener exhaustivas las listas de dependencias de `useEffect`, `useCallback`
  y otros hooks equivalentes.
- Evitar duplicar lógica en componentes y selectores; extraer lógica compartida
  a helpers, hooks o utilidades apropiadas.

## Checklist previo a la entrega

Antes de finalizar, el agente MUST:

1. Revisar los cambios para detectar nuevos code smells, duplicación, nombres
   inválidos, tipos inseguros, excepciones genéricas y logging prohibido.
2. Confirmar que se conservaron las convenciones y la arquitectura existente.
3. Ejecutar `./gradlew test` cuando haya cambios en backend o tests Java.
4. Ejecutar el lint, type-check o tests configurados del frontend cuando haya
   cambios en TypeScript/React.
5. Ejecutar o revisar el escaneo de SonarCloud disponible y confirmar que no se
   rompe el Quality Gate.
6. Informar explícitamente cualquier verificación que no pueda ejecutarse y su
   motivo.
