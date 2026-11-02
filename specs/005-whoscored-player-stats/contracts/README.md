# Contratos de la feature

Esta feature no agrega endpoints REST ni un scheduler. El contrato publico de
la aplicacion es el servicio invocable internamente y el puerto reemplazable de
scraping. La persistencia y sus constraints estan descritas en
[../data-model.md](../data-model.md).

- [scraping-port.md](scraping-port.md): DTOs y operaciones que cruzan la
  frontera entre aplicacion y adapter.
