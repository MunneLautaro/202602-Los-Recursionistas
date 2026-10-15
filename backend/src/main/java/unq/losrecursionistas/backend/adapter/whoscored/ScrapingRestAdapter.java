package unq.losrecursionistas.backend.adapter.whoscored;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.springframework.stereotype.Component;
import unq.losrecursionistas.backend.adapter.whoscored.dto.EquipoWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.AparicionWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.JugadorWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.LigaWhoscoredDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ScrapingRestAdapter implements ScrapingPort {
    private final ScrapingConfiguration configuration;
    private final ObjectMapper objectMapper;

    public ScrapingRestAdapter(ScrapingConfiguration configuration, ObjectMapper objectMapper) {
        this.configuration = configuration;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<LigaWhoscoredDto> descubrirLigas() {
        PageData data = load("/Regions", "descubrir ligas");
        List<LigaWhoscoredDto> result = new ArrayList<>();
        for (JsonNode node : findObjects(data.json(), "leagueId", "id")) {
            Long id = number(node, "leagueId", "id");
            String nombre = text(node, "name", "leagueName");
            if (id != null && nombre != null) {
                result.add(new LigaWhoscoredDto(id, nombre, text(node, "slug", "url"), text(node, "season")));
            }
        }
        return distinctLigas(result);
    }

    @Override
    public List<EquipoWhoscoredDto> descubrirEquipos(Long ligaWhoscoredId) {
        requireId(ligaWhoscoredId, "liga");
        PageData data = load("/Regions/" + ligaWhoscoredId + "/Tournaments", "descubrir equipos");
        List<EquipoWhoscoredDto> result = new ArrayList<>();
        for (JsonNode node : findObjects(data.json(), "teamId", "team")) {
            Long id = number(node, "teamId", "id");
            String nombre = text(node, "name", "teamName");
            if (id != null && nombre != null) {
                result.add(new EquipoWhoscoredDto(id, nombre, text(node, "slug", "url"), ligaWhoscoredId));
            }
        }
        return result.stream().distinct().toList();
    }

    @Override
    public List<JugadorWhoscoredDto> descubrirJugadores(Long equipoWhoscoredId) {
        requireId(equipoWhoscoredId, "equipo");
        PageData data = load("/Teams/" + equipoWhoscoredId + "/Show", "descubrir jugadores");
        List<JugadorWhoscoredDto> result = new ArrayList<>();
        for (JsonNode node : findObjects(data.json(), "playerId", "player")) {
            Long id = number(node, "playerId", "id");
            String nombre = text(node, "name", "playerName");
            if (id != null && nombre != null) {
                result.add(new JugadorWhoscoredDto(id, nombre, text(node, "slug", "url"),
                        date(node, "dateOfBirth", "birthDate"), text(node, "nationality", "countryName"),
                        equipoWhoscoredId, text(node, "teamName"), strings(node, "positions", "position")));
            }
        }
        return result.stream().distinct().toList();
    }

    @Override
    public List<AparicionWhoscoredDto> obtenerApariciones(Long jugadorWhoscoredId) {
        requireId(jugadorWhoscoredId, "jugador");
        PageData data = load("/Players/" + jugadorWhoscoredId + "/History", "obtener apariciones");
        Map<Long, AparicionBuilder> apariciones = new LinkedHashMap<>();
        for (JsonNode node : findObjects(data.json(), "matchId", "fixtureId", "gameId")) {
            Long partidoId = number(node, "matchId", "fixtureId", "gameId");
            if (partidoId == null) {
                continue;
            }
            AparicionBuilder builder = apariciones.computeIfAbsent(partidoId, AparicionBuilder::new);
            builder.merge(node);
        }
        return apariciones.values().stream().map(AparicionBuilder::build).toList();
    }

    private PageData load(String path, String fase) {
        List<String> jsonResponses = new CopyOnWriteArrayList<>();
        String url = configuration.getBaseUrl() + path;
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                     .setHeadless(configuration.isHeadless()));
             BrowserContext context = browser.newContext(new Browser.NewContextOptions().setUserAgent(configuration.getUserAgent()));
             Page page = context.newPage()) {
            page.setDefaultNavigationTimeout(configuration.getNavigationTimeoutMs());
            page.onResponse(response -> {
                try {
                    String contentType = response.headerValue("content-type");
                    if (contentType != null && contentType.contains("json")) {
                        jsonResponses.add(response.text());
                    }
                } catch (RuntimeException ignored) {
                    // A response can be closed by the browser while the page is navigating.
                }
            });
            page.navigate(url, new Page.NavigateOptions().setTimeout(configuration.getNavigationTimeoutMs()));
            String html = page.content();
            List<JsonNode> json = new ArrayList<>();
            for (String response : jsonResponses) {
                try {
                    json.add(objectMapper.readTree(response));
                } catch (Exception ignored) {
                    // Non-JSON responses are ignored; embedded JSON is tried below.
                }
            }
            for (String script : page.locator("script").allTextContents()) {
                try {
                    if (script.contains("\"playerId\"") || script.contains("\"teamId\"") || script.contains("\"leagueId\"")) {
                        json.add(objectMapper.readTree(script.trim()));
                    }
                } catch (Exception ignored) {
                    // Scripts often contain JavaScript around the embedded JSON.
                }
            }
            return new PageData(json, html);
        } catch (Exception exception) {
            throw new ScrapingException("No se pudo obtener datos de WhoScored", url, fase, exception);
        }
    }

    private List<JsonNode> findObjects(List<JsonNode> roots, String... keys) {
        List<JsonNode> result = new ArrayList<>();
        for (JsonNode root : roots) {
            collect(root, result, keys);
        }
        return result;
    }

    private void collect(JsonNode node, List<JsonNode> result, String... keys) {
        if (node.isObject()) {
            boolean matches = false;
            for (String key : keys) {
                matches |= node.has(key);
            }
            if (matches) {
                result.add(node);
            }
            node.elements().forEachRemaining(child -> collect(child, result, keys));
        } else if (node.isArray()) {
            node.elements().forEachRemaining(child -> collect(child, result, keys));
        }
    }

    private static String text(JsonNode node, String... names) {
        for (String name : names) {
            JsonNode value = node.get(name);
            if (value != null && !value.isNull() && value.isValueNode()) {
                return value.asText();
            }
        }
        return null;
    }

    private static Long number(JsonNode node, String... names) {
        String value = text(node, names);
        try {
            return value == null ? null : Long.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static LocalDate date(JsonNode node, String... names) {
        try {
            String value = text(node, names);
            return value == null ? null : LocalDate.parse(value.substring(0, 10));
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static List<String> strings(JsonNode node, String... names) {
        for (String name : names) {
            JsonNode value = node.get(name);
            if (value != null && value.isArray()) {
                List<String> result = new ArrayList<>();
                value.forEach(item -> result.add(item.isValueNode() ? item.asText() : text(item, "name")));
                return result;
            }
        }
        return List.of();
    }

    private static final class AparicionBuilder {
                private final Long partidoId;
                private LocalDate fecha;
                private String torneo;
                private Long torneoId;
                private Long equipoId;
                private String equipoNombre;
                private Long rivalId;
                private String rivalNombre;
                private String localVisitante;
                private Integer minutos;
                private BigDecimal rating;
                private final Map<String, Object> crudas = new LinkedHashMap<>();

                private AparicionBuilder(Long partidoId) {
                    this.partidoId = partidoId;
                }

                private void merge(JsonNode node) {
                    fecha = firstDate(fecha, date(node, "date", "matchDate", "gameDate"));
                    torneo = first(torneo, text(node, "tournamentName", "competitionName", "tournament", "competition"));
                    torneoId = first(torneoId, number(node, "tournamentId", "competitionId"));
                    equipoId = first(equipoId, number(node, "teamId", "playerTeamId"));
                    equipoNombre = first(equipoNombre, text(node, "teamName", "playerTeam"));
                    rivalId = first(rivalId, number(node, "opponentId", "rivalId"));
                    rivalNombre = first(rivalNombre, text(node, "opponentName", "rivalName"));
                    localVisitante = first(localVisitante, text(node, "venue", "homeAway", "location"));
                    minutos = first(minutos, integer(node, "minutes", "minutesPlayed", "mins"));
                    rating = first(rating, decimal(node, "rating", "playerRating"));
                    node.properties().forEach(entry -> {
                        if (entry.getValue().isValueNode()) {
                            crudas.putIfAbsent(entry.getKey(), value(entry.getValue()));
                        }
                    });
                }

                private AparicionWhoscoredDto build() {
                    return new AparicionWhoscoredDto(partidoId, fecha, torneo, torneoId, equipoId, equipoNombre,
                            rivalId, rivalNombre, localVisitante, minutos, rating, crudas);
                }

                private static <T> T first(T current, T candidate) {
                    return current != null ? current : candidate;
                }

                private static LocalDate firstDate(LocalDate current, LocalDate candidate) {
                    return current != null ? current : candidate;
                }
            }

            private static Integer integer(JsonNode node, String... names) {
                Long value = number(node, names);
                return value == null ? null : value.intValue();
            }

            private static BigDecimal decimal(JsonNode node, String... names) {
                String value = text(node, names);
                try {
                    return value == null ? null : new BigDecimal(value);
                } catch (NumberFormatException exception) {
                    return null;
                }
            }

            private static Object value(JsonNode node) {
                if (node.isBoolean()) return node.booleanValue();
                if (node.isIntegralNumber()) return node.longValue();
                if (node.isFloatingPointNumber()) return node.doubleValue();
                return node.asText();
            }
    private static List<LigaWhoscoredDto> distinctLigas(List<LigaWhoscoredDto> ligas) {
        return ligas.stream().distinct().toList();
    }

    private static void requireId(Long id, String tipo) {
        if (id == null || id <= 0) {
            throw new ScrapingException("El identificador de " + tipo + " es obligatorio", null, "validacion");
        }
    }

    private record PageData(List<JsonNode> json, String html) {
    }

}
