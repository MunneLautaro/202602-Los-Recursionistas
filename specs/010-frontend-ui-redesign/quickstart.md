# Guía de validación rápida

## Prerrequisitos

- Node.js compatible con el proyecto.
- Yarn disponible.
- Backend ejecutándose en la URL de `VITE_API_URL` o en
  `http://localhost:8080`.
- Usuario de prueba con credenciales y datos de jugadores, ranking, órdenes y
  cuenta.

## Instalación

Desde `frontend/`, instalar únicamente las dependencias aprobadas:

```bash
yarn add lucide-react react-hook-form zod @hookform/resolvers
```

No usar `npm install` ni agregar librerías de componentes.

La implementación usa `react-hook-form` con resolvers Zod en autenticación,
órdenes y recargas; los errores del servidor se muestran sin reiniciar los
valores ingresados. Las llamadas HTTP permanecen encapsuladas en `fetchClient`
y los módulos `features/*/api`.

## Verificaciones estáticas

```bash
yarn lint
yarn build
```

El build debe completar `tsc -b` y `vite build`. Revisar que no existan
`any`, `fetch` directo en componentes, CSS inline ni imports de iconos fuera
de `lucide-react`.

## Escenarios manuales

1. Abrir `/` en desktop y mobile. Confirmar propósito, CTAs a jugadores,
   ranking y mercado, y CTA autenticado según sesión.
2. En `/jugadores`, escribir un nombre y detenerse 400 ms. Confirmar
   actualización automática, contador, chip removible, paginación, skeleton,
   vacío y reintento.
3. Abrir un jugador, validar estadísticas, rango de cotizaciones inválido,
   paginación, Escape y retorno del foco al elemento disparador.
4. En `/ranking`, cambiar liga y posición. Confirmar actualización automática
   y que cada variación se entienda por icono, signo y texto, también sin color.
5. En `/mercado`, probar filtros, una orden válida, cantidad cero/decimal y
   rechazo del backend. Confirmar bloqueo de doble envío y conservación de
   valores.
6. En `/cuenta`, probar balance, recarga válida/inválida, órdenes y
   transacciones. Confirmar estados vacíos, feedback y actualización posterior
   a éxito.
7. En login y registro, probar errores por campo, error general, valores
   iniciales y botón deshabilitado durante envío.
8. Con teclado y lector de pantalla, revisar menú móvil, labels, foco,
   `aria-expanded`, `aria-invalid`, `aria-describedby`, regiones dinámicas y
   botones solo-icono.
9. Emular tema claro y oscuro y viewports mobile/tablet/desktop. Confirmar
   contraste WCAG AA y ausencia de scroll horizontal.

## Referencias

- Entidades y reglas: [data-model.md](./data-model.md).
- Interfaces compartidas: [contracts/](./contracts/).
- Decisiones y auditoría: [research.md](./research.md).

El escaneo SonarCloud debe ejecutarse si está disponible en el entorno. Si no
está disponible, se documentará explícitamente junto con el motivo.

Como trabajo futuro, se puede automatizar la matriz de escenarios responsive y
contraste WCAG con pruebas de navegador; queda fuera del alcance de esta
entrega.
