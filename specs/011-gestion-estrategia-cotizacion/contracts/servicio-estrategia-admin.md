# Interfaz Java

```java
public interface EstrategiaAdminService {
    EstadoEstrategiaCotizacion consultar();

    ResultadoCambioEstrategia cambiar(String estrategia, Usuario usuarioAdmin);
}
```

El service retornará entidades o tipos de caso de uso, no objetos HTTP. Debe incluir la estrategia activa, listado fijo de estrategias, origen y ultimo cambio.

Responsabilidades:
- validar el codigo solicitado;
- comprobar rol insuficiente;
- comprobar que no existe recálculo en curso;
- resolver activo efectivo usando configuracion persistida y luego fallback;
- aplicar idempotencia;
- ejecutar actualizacion y auditoria en una misma transaccion.

La implementacion no debe contener mapeo a DTO REST ni consultar `HttpServletRequest`.

