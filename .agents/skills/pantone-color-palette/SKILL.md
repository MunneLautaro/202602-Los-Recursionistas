---
name: pantone-color-palette
description: >-
  Establece las directrices y reglas de diseño visual basadas en la paleta de colores Pantone del proyecto.
  Usar siempre que se diseñen, modifiquen o agreguen componentes UI, estilos CSS o temas visuales en el frontend.
---

# Directrices de la Paleta de Colores Pantone

Esta skill define los colores oficiales y las reglas de diseño de interfaz que el agente y los desarrolladores deben seguir en todo el frontend.

## 🎨 Paleta de Colores Oficial (Pantone)

| Nombre               | Pantone Code | Hex       | Variables CSS                               | Uso Principal                                                                                     |
| :------------------- | :----------- | :-------- | :------------------------------------------ | :------------------------------------------------------------------------------------------------ |
| **Sun Glare**        | 13-0663 TSX  | `#DFFF00` | `--pantone-sun-glare`, `--primary`          | Botones de acción principal (CTA), highlights, badges destacados, acentos activos en modo oscuro. |
| **Exuberant Orange** | 17-1363 TSX  | `#FF5528` | `--pantone-exuberant-orange`, `--secondary` | Badges de alerta, botones secundarios de alto impacto, estados de advertencia/notificación.       |
| **Blue Violet**      | 2725 C       | `#6952D1` | `--pantone-blue-violet`, `--accent`         | Enlaces, acentos de marca, bordes activos, iconos interactivos en modo claro.                     |
| **Cloud Dancer**     | 11-4201 TPG  | `#F2EFE9` | `--pantone-cloud-dancer`, `--bg`            | Fondo principal (Modo Claro), fondo de tarjetas en modo claro, texto primario en modo oscuro.     |
| **Darkest Hour**     | 20-0199 TPM  | `#262425` | `--pantone-darkest-hour`, `--text`          | Fondo principal (Modo Oscuro), texto primario (Modo Claro), encabezados y elementos oscuros.      |

---

## 🛠️ Uso en CSS / Tailwind

Al agregar o modificar estilos UI en `frontend`:

1. **Utilizar Variables CSS Locales:**
   Prefiere siempre el uso de las variables definidas en `index.css`:

   ```css
   color: var(--text);
   background-color: var(--bg);
   border-color: var(--border);
   ```

2. **Extensión en Tailwind / Clases Personalizadas:**
   Si se requiere Tailwind arbitrary values o config:
   - `bg-[#DFFF00]` o `bg-[var(--pantone-sun-glare)]`
   - `text-[#262425]` o `text-[var(--pantone-darkest-hour)]`
   - `border-[#6952D1]` o `border-[var(--pantone-blue-violet)]`

---

## 🎯 Reglas de Contraste y Accesibilidad

- **Sun Glare (`#DFFF00`)**: Debe combinarse con texto oscuro (`#262425` - Darkest Hour) para garantizar legibilidad y alto contraste.
- **Cloud Dancer (`#F2EFE9`)**: Usado como canvas claro; los textos sobre este fondo deben utilizar `#262425`.
- **Blue Violet (`#6952D1`)**: Funciona adecuadamente con textos en blanco/Cloud Dancer.
