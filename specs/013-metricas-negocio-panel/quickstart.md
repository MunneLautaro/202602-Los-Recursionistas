# Quickstart: Validar métricas de negocio y panel de observabilidad

**Feature**: [spec.md](spec.md) | **Contratos**: [observabilidad-admin.md](contracts/observabilidad-admin.md), [metricas-negocio.md](contracts/metricas-negocio.md)

Esta guía usa PowerShell en Windows. Reutiliza la función `Get-CodigoEstado` y las variables `$base`,
`$hAdmin` y `$hUsuario` del [quickstart de la feature 012](../012-observabilidad-actuator-prometheus/quickstart.md#2-preparar-las-variables-de-la-sesión).

## Prerrequisitos

- PostgreSQL con datos de mercado y cotizaciones. El SQL nativo se valida contra PostgreSQL, no contra H2.
- En `backend/.env`, `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus`
  (feature 012).

## 1. Tests y build

```powershell
cd backend;  .\gradlew.bat clean test        # verde, incluye ArquitecturaCapasTest y los tests nuevos
cd ..\frontend; yarn lint; yarn build         # sin errores
```

## 2. Acceso a los endpoints admin

```powershell
foreach ($r in "/api/admin/observabilidad/negocio", "/api/admin/observabilidad/tecnico") {
  "{0,-40} anon={1} usuario={2} admin={3}" -f $r, (Get-CodigoEstado "$base$r"), (Get-CodigoEstado "$base$r" $hUsuario), (Get-CodigoEstado "$base$r" $hAdmin)
}
Get-CodigoEstado "$base/api/admin/observabilidad/negocio?dias=15" $hAdmin   # 400
```

**Esperado**: anónimo 401, usuario 403 y admin 200; `dias=15` responde 400.

## 3. Generar actividad y verificar las métricas en vivo

1. Como usuario común, crear con `POST /orders/buy` una orden de compra con saldo suficiente y otra con
   una cantidad que supere el saldo.
2. Crear con `POST /orders/sell` una orden de venta compatible para que se empareje.
3. Desde el panel admin existente, ejecutar "Recalcular cotizaciones".

```powershell
$p = (Invoke-WebRequest "$base/actuator/prometheus" -Headers $hAdmin -UseBasicParsing).Content
$p -split "`n" | Select-String '^(mercado|cotizacion)_'
```

**Esperado**:
- Aparecen todas las series del [contrato](contracts/metricas-negocio.md) y todas tienen `application=`.
- `mercado_ordenes_rechazadas_total{motivo="saldo_insuficiente"}` vale al menos 1.
- No hay `_bucket` ni `quantile`.

## 4. Totales históricos contra la base (SC-002)

```powershell
$n = Invoke-RestMethod "$base/api/admin/observabilidad/negocio?dias=30" -Headers $hAdmin
$n.periodo; $n.serieMercado.Count   # 30
```

Comparar con SQL directo en PostgreSQL sobre el mismo rango:

```sql
SELECT tipo, COUNT(*) FROM ordenes
WHERE NOT es_liquidez_inicial
  AND CAST(fecha_creacion AT TIME ZONE 'America/Argentina/Buenos_Aires' AS DATE)
      BETWEEN CURRENT_DATE - 29 AND CURRENT_DATE
GROUP BY tipo;

SELECT COUNT(*), SUM(total) FROM transacciones_mercado
WHERE CAST(ejecutada_en AT TIME ZONE 'America/Argentina/Buenos_Aires' AS DATE)
      BETWEEN CURRENT_DATE - 29 AND CURRENT_DATE;
```

**Esperado**: coincidencia exacta con `periodo.ordenesCompra`, `ordenesVenta`, `emparejamientos` y
`volumenCreditos`.

## 5. Resumen técnico

```powershell
$t = Invoke-RestMethod "$base/api/admin/observabilidad/tecnico" -Headers $hAdmin
$t.salud; $t.requests.endpoints | Format-Table; $t.jvm; $t.poolConexiones
```

**Esperado**:
- Los componentes de salud no tienen `details`.
- `endpoints` no incluye `/actuator/**` ni `/api/admin/observabilidad/**`.

## 6. Panel en el navegador

1. `cd frontend; yarn dev` e iniciar sesión como admin en `http://localhost:5173`.
2. En **Administración**, entrar a **Observabilidad** (`/admin/observabilidad`).
3. Verificar:
   - **Mercado**: tarjetas, gráfico de órdenes compra/venta y gráfico de emparejamientos y volumen. Los
     días sin actividad aparecen en cero.
   - **Cotización**: estrategia activa, contadores de recálculo rotulados "desde el último reinicio", y
     gráfico de jugadores cotizados y variación.
   - **Técnico**: salud por componente, top de endpoints con tasa de error y latencia, y gráficos de
     memoria, CPU y pool que crecen con cada refresco.
   - Al cambiar el período a 7 o 90 días, los gráficos cambian.
   - Después de crear una orden, el panel la refleja en 30 segundos o menos sin recargar (SC-003).
4. Con un usuario común, `/admin/observabilidad` no muestra el panel.
5. **Rendimiento (SC-006)**:
   `(Measure-Command { Invoke-RestMethod "$base/api/admin/observabilidad/negocio?dias=90" -Headers $hAdmin }).TotalMilliseconds`
   debe dar menos de 3000. En el navegador, la pestaña Network al cargar el panel con 90 días también
   debe quedar por debajo de 3 s.

## 7. Errores por sección

Detener PostgreSQL con el panel abierto.

**Esperado**:
- El panel muestra el banner "Servicio no disponible (estado: DOWN)".
- La sesión del admin **no** se cierra.
- No se piden los endpoints protegidos (no hay 401 en Network).
- Al volver a levantar la base, todo se recupera en el siguiente refresco.
