import { Link } from "react-router-dom";
import {
  GraficoCotizacion,
  GraficoMercado,
  GraficoRecursos,
  PanelSaludTecnica,
  SelectorPeriodo,
  TarjetaMetrica,
  formatearCreditos,
  formatearDuracion,
  formatearNumero,
  formatearVariacion,
  useObservabilidad,
} from "../features/admin";

const DESDE_REINICIO = "desde el último reinicio";

const estilos = {
  main: "mx-auto grid max-w-6xl gap-8 text-left",
  cabecera:
    "ui-card overflow-hidden bg-[radial-gradient(60%_120%_at_0%_0%,rgba(98,56,222,0.42),transparent_70%),radial-gradient(50%_120%_at_100%_100%,rgba(92,255,210,0.16),transparent_70%)] p-8 sm:p-10",
  cabeceraFila: "mt-6 flex flex-wrap items-center justify-between gap-4",
  descripcion: "mt-4 max-w-2xl text-base font-light text-white/60",
  actualizado: "text-xs text-white/40",
  aviso: "ui-card border-coral/40 p-5 text-sm text-coral",
  seccion: "ui-card grid gap-5 p-6 sm:p-8",
  tituloSeccion: "text-2xl font-semibold tracking-tight text-white",
  subtitulo: "text-sm text-white/50",
  tarjetas: "grid grid-cols-2 gap-3 md:grid-cols-3 lg:grid-cols-6",
  estado: "text-sm text-white/50",
  error: "text-sm text-coral",
} as const;

export function ObservabilidadPage() {
  const {
    periodo,
    cambiarPeriodo,
    negocio,
    errorNegocio,
    cargandoNegocio,
    tecnico,
    errorTecnico,
    cargandoTecnico,
    muestrasTecnicas,
    estadoServicio,
    ultimaActualizacion,
  } = useObservabilidad();

  return (
    <main className={estilos.main}>
      <header className={estilos.cabecera}>
        <p className="ui-eyebrow mb-4 flex items-center gap-3">
          <span className="ui-dot" aria-hidden="true" />
          <Link to="/admin" className="hover:text-white">Administración</Link> / Observabilidad
        </p>
        <h1 className="ui-page-title">Observabilidad</h1>
        <p className={estilos.descripcion}>
          Actividad del mercado, comportamiento de la cotización y salud técnica de la
          plataforma. Se actualiza automáticamente cada 30 segundos.
        </p>
        <div className={estilos.cabeceraFila}>
          <SelectorPeriodo valor={periodo} onChange={cambiarPeriodo} />
          {ultimaActualizacion ? (
            <p className={estilos.actualizado} aria-live="polite">
              Última actualización: {ultimaActualizacion.toLocaleTimeString("es-AR")}
            </p>
          ) : null}
        </div>
      </header>

      {estadoServicio ? (
        <p role="alert" className={estilos.aviso}>
          Servicio no disponible (estado: {estadoServicio}). El panel se recuperará
          automáticamente cuando el servicio vuelva a estar disponible.
        </p>
      ) : null}

      <section className={estilos.seccion} aria-labelledby="mercado-titulo">
        <div>
          <h2 id="mercado-titulo" className={estilos.tituloSeccion}>Mercado</h2>
          <p className={estilos.subtitulo}>Totales de los últimos {periodo} días y estado actual.</p>
        </div>
        {cargandoNegocio && !negocio ? <p role="status" className={estilos.estado}>Cargando métricas de mercado...</p> : null}
        {errorNegocio ? <p role="alert" className={estilos.error}>{errorNegocio}</p> : null}
        {negocio ? (
          <>
            <div className={estilos.tarjetas}>
              <TarjetaMetrica etiqueta="Órdenes de compra" valor={formatearNumero(negocio.periodo.ordenesCompra)} />
              <TarjetaMetrica etiqueta="Órdenes de venta" valor={formatearNumero(negocio.periodo.ordenesVenta)} />
              <TarjetaMetrica etiqueta="Emparejamientos" valor={formatearNumero(negocio.periodo.emparejamientos)} />
              <TarjetaMetrica etiqueta="Volumen operado" valor={formatearCreditos(negocio.periodo.volumenCreditos)} />
              <TarjetaMetrica
                etiqueta="Tokens en circulación"
                valor={formatearNumero(negocio.actual.tokensEnCirculacion)}
                nota="actual"
              />
              <TarjetaMetrica
                etiqueta="Rechazos por saldo"
                valor={formatearNumero(negocio.enVivo.rechazosSaldoInsuficiente)}
                nota={DESDE_REINICIO}
                tono={negocio.enVivo.rechazosSaldoInsuficiente > 0 ? "alerta" : "neutro"}
              />
            </div>
            <GraficoMercado serie={negocio.serieMercado} />
          </>
        ) : null}
      </section>

      <section className={estilos.seccion} aria-labelledby="cotizacion-titulo">
        <div>
          <h2 id="cotizacion-titulo" className={estilos.tituloSeccion}>Cotización</h2>
          <p className={estilos.subtitulo}>Recálculos y evolución de los valores de los jugadores.</p>
        </div>
        {cargandoNegocio && !negocio ? <p role="status" className={estilos.estado}>Cargando métricas de cotización...</p> : null}
        {errorNegocio ? <p role="alert" className={estilos.error}>{errorNegocio}</p> : null}
        {negocio ? (
          <>
            <div className={estilos.tarjetas}>
              <TarjetaMetrica etiqueta="Estrategia activa" valor={negocio.actual.estrategiaActiva.nombre} tono="positivo" />
              <TarjetaMetrica
                etiqueta="Recálculos exitosos"
                valor={formatearNumero(negocio.enVivo.recalculosExitosos)}
                nota={DESDE_REINICIO}
              />
              <TarjetaMetrica
                etiqueta="Recálculos fallidos"
                valor={formatearNumero(negocio.enVivo.recalculosFallidos)}
                nota={DESDE_REINICIO}
                tono={negocio.enVivo.recalculosFallidos > 0 ? "alerta" : "neutro"}
              />
              <TarjetaMetrica
                etiqueta="Duración (último / prom.)"
                valor={`${formatearDuracion(negocio.enVivo.duracionUltimoRecalculoMs)} / ${formatearDuracion(negocio.enVivo.duracionPromedioRecalculoMs)}`}
                nota={DESDE_REINICIO}
              />
              <TarjetaMetrica
                etiqueta="Cotizados (último)"
                valor={formatearNumero(negocio.enVivo.jugadoresCotizadosUltimoRecalculo)}
                nota={DESDE_REINICIO}
              />
              <TarjetaMetrica
                etiqueta="Variación (último)"
                valor={formatearVariacion(negocio.enVivo.variacionPromedioUltimoRecalculo)}
                nota={DESDE_REINICIO}
              />
            </div>
            <div className={estilos.tarjetas}>
              <TarjetaMetrica
                etiqueta="Cotizaciones del período"
                valor={formatearNumero(negocio.periodo.cotizacionesCalculadas)}
              />
              <TarjetaMetrica
                etiqueta="Variación del período"
                valor={formatearVariacion(negocio.periodo.variacionPromedio)}
                nota="ponderada"
              />
            </div>
            <GraficoCotizacion serie={negocio.serieCotizacion} />
          </>
        ) : null}
      </section>

      <section className={estilos.seccion} aria-labelledby="tecnico-titulo">
        <div>
          <h2 id="tecnico-titulo" className={estilos.tituloSeccion}>Técnico</h2>
          <p className={estilos.subtitulo}>Salud, requests por endpoint, recursos de la JVM y pool de conexiones.</p>
        </div>
        {cargandoTecnico && !tecnico ? <p role="status" className={estilos.estado}>Cargando métricas técnicas...</p> : null}
        {errorTecnico ? <p role="alert" className={estilos.error}>{errorTecnico}</p> : null}
        {tecnico ? (
          <>
            <PanelSaludTecnica tecnico={tecnico} />
            <GraficoRecursos tecnico={tecnico} muestras={muestrasTecnicas} />
          </>
        ) : null}
      </section>
    </main>
  );
}
