package unq.losrecursionistas.backend.adapter.whoscored;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import unq.losrecursionistas.backend.adapter.whoscored.dto.AparicionWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.EquipoWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.JugadorWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.LigaWhoscoredDto;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ScrapingRestAdapter implements ScrapingPort {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScrapingRestAdapter.class);

    private static final Map<Long, String> TOURNAMENT_PATHS = Map.of(
            252L, "/Regions/252/Tournaments/2/England-Premier-League",
            206L, "/Regions/206/Tournaments/4/Spain-LaLiga",
            81L, "/Regions/81/Tournaments/3/Germany-Bundesliga",
            108L, "/Regions/108/Tournaments/5/Italy-Serie-A",
            74L, "/Regions/74/Tournaments/22/France-Ligue-1"
    );

    private static final List<LigaWhoscoredDto> LIGAS_TOP = List.of(
            new LigaWhoscoredDto(252L, "Premier League", "/Regions/252/Tournaments/2/England-Premier-League", "2025/2026"),
            new LigaWhoscoredDto(206L, "LaLiga", "/Regions/206/Tournaments/4/Spain-LaLiga", "2025/2026"),
            new LigaWhoscoredDto(81L, "Bundesliga", "/Regions/81/Tournaments/3/Germany-Bundesliga", "2025/2026"),
            new LigaWhoscoredDto(108L, "Serie A", "/Regions/108/Tournaments/5/Italy-Serie-A", "2025/2026"),
            new LigaWhoscoredDto(74L, "Ligue 1", "/Regions/74/Tournaments/22/France-Ligue-1", "2025/2026")
    );

    private final ScrapingConfiguration configuration;
    private final ObjectMapper objectMapper;

    public ScrapingRestAdapter(ScrapingConfiguration configuration, ObjectMapper objectMapper) {
        this.configuration = configuration;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<LigaWhoscoredDto> descubrirLigas() {
        List<LigaWhoscoredDto> result = new ArrayList<>(LIGAS_TOP);
        try {
            PageData data = load("/Regions", "descubrir ligas");
            for (JsonNode node : findObjects(data.json(), "leagueId", "id")) {
                Long id = number(node, "leagueId", "id");
                String nombre = text(node, "name", "leagueName");
                if (id != null && nombre != null) {
                    result.add(new LigaWhoscoredDto(id, nombre, text(node, "slug", "url"), text(node, "season")));
                }
            }
        } catch (Exception ex) {
            LOGGER.info("No se obtuvo índice dinámico de /Regions, usando ligas configuradas de WhoScored ({} ligas)", LIGAS_TOP.size());
        }
        return distinctLigas(result);
    }

    @Override
    public List<EquipoWhoscoredDto> descubrirEquipos(Long ligaWhoscoredId) {
        requireId(ligaWhoscoredId, "liga");
        String path = TOURNAMENT_PATHS.getOrDefault(ligaWhoscoredId, "/Regions/" + ligaWhoscoredId + "/Tournaments");
        PageData data = load(path, "descubrir equipos");
        Map<Long, EquipoWhoscoredDto> result = new LinkedHashMap<>();

        // 1. Parsear objetos JSON si están presentes
        for (JsonNode node : findObjects(data.json(), "teamId", "team")) {
            Long id = number(node, "teamId", "id");
            String nombre = text(node, "name", "teamName");
            if (id != null && nombre != null) {
                result.put(id, new EquipoWhoscoredDto(id, nombre, text(node, "slug", "url"), ligaWhoscoredId));
            }
        }

        // 2. Parsear enlaces HTML del DOM (/teams/{id}/show/{slug})
        if (data.html() != null) {
            Pattern pattern = Pattern.compile("(?i)href=[\"']/teams/(\\d+)/(?:show/)?([^\"'/?#]+)[\"'][^>]*>(.*?)</a>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(data.html());
            while (matcher.find()) {
                try {
                    Long id = Long.valueOf(matcher.group(1));
                    String slugPart = matcher.group(2);
                    String rawText = matcher.group(3).replaceAll("<[^>]*>", "").trim();
                    String nombre = rawText.replaceAll("^\\d+\\s*", "").trim();
                    if (nombre.isBlank() || nombre.equalsIgnoreCase("teammate") || nombre.toLowerCase().contains("statistics")) {
                        nombre = slugPart.replaceAll("^[a-z]+-", "").replace("-", " ").trim();
                    }
                    if (!nombre.isBlank() && !nombre.equalsIgnoreCase("teammate") && !nombre.toLowerCase().contains("statistics")) {
                        result.putIfAbsent(id, new EquipoWhoscoredDto(id, nombre, "/teams/" + id + "/show/" + slugPart, ligaWhoscoredId));
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        return new ArrayList<>(result.values());
    }

    @Override
    public List<JugadorWhoscoredDto> descubrirJugadores(Long equipoWhoscoredId) {
        requireId(equipoWhoscoredId, "equipo");
        PageData data = load("/teams/" + equipoWhoscoredId + "/show", "descubrir jugadores");
        Map<Long, JugadorWhoscoredDto> result = new LinkedHashMap<>();

        // 1. Parsear objetos JSON si están presentes
        for (JsonNode node : findObjects(data.json(), "playerId", "player")) {
            Long id = number(node, "playerId", "id");
            String nombre = text(node, "name", "playerName");
            if (id != null && nombre != null) {
                result.put(id, new JugadorWhoscoredDto(id, nombre, text(node, "slug", "url"),
                        date(node, "dateOfBirth", "birthDate"), text(node, "nationality", "countryName"),
                        equipoWhoscoredId, text(node, "teamName"), strings(node, "positions", "position")));
            }
        }

        // 2. Parsear enlaces HTML del DOM (/players/{id}/show/{slug})
        if (data.html() != null) {
            Pattern pattern = Pattern.compile("(?i)href=[\"']/players/(\\d+)/(?:show/)?([^\"'/?#]+)[\"'][^>]*>(.*?)</a>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(data.html());
            while (matcher.find()) {
                try {
                    Long id = Long.valueOf(matcher.group(1));
                    String slugPart = matcher.group(2);
                    String rawText = matcher.group(3).replaceAll("<[^>]*>", "").trim();
                    String nombre = rawText.replaceAll("^\\d+\\s*", "").trim();
                    if (nombre.isBlank()) {
                        nombre = slugPart.replace("-", " ").trim();
                    }
                    if (!nombre.isBlank()) {
                        result.putIfAbsent(id, new JugadorWhoscoredDto(id, nombre, "/players/" + id + "/show/" + slugPart,
                                null, null, equipoWhoscoredId, null, List.of()));
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        return new ArrayList<>(result.values());
    }

    @Override
    public List<AparicionWhoscoredDto> obtenerApariciones(Long jugadorWhoscoredId) {
        requireId(jugadorWhoscoredId, "jugador");
        List<AparicionWhoscoredDto> apariciones = obtenerAparicionesDesdeRuta("/players/" + jugadorWhoscoredId + "/show", "obtener apariciones");
        if (apariciones.isEmpty()) {
            try {
                apariciones = obtenerAparicionesDesdeRuta("/players/" + jugadorWhoscoredId + "/matchstatistics", "obtener apariciones matchstatistics");
            } catch (Exception ex) {
                LOGGER.debug("No se obtuvieron apariciones desde /matchstatistics para jugador {}: {}", jugadorWhoscoredId, ex.getMessage());
            }
        }
        return apariciones;
    }

    @Override
    public List<AparicionWhoscoredDto> obtenerAparicionesTemporada(Long jugadorWhoscoredId, String slug) {
        requireId(jugadorWhoscoredId, "jugador");
        if (slug == null || slug.isBlank()) {
            throw new ScrapingException("El slug del jugador es obligatorio", null, "obtener estadísticas de temporada", null);
        }
        return obtenerAparicionesDesdeRuta(
                "/players/" + jugadorWhoscoredId + "/matchstatistics/" + encodePathSegment(slug),
                "obtener estadísticas de temporada");
    }

    private List<AparicionWhoscoredDto> obtenerAparicionesDesdeRuta(String ruta, String fase) {
        PageData data = load(ruta, fase);
        Map<Long, AparicionBuilder> apariciones = new LinkedHashMap<>();

        // 1. Desde JSON — campos explícitos de partido
        for (JsonNode node : findObjects(data.json(), "matchId", "fixtureId", "gameId")) {
            Long partidoId = number(node, "matchId", "fixtureId", "gameId");
            if (partidoId == null) {
                continue;
            }
            AparicionBuilder builder = apariciones.computeIfAbsent(partidoId, AparicionBuilder::new);
            builder.merge(node);
        }

        // 1b. Desde JSON — objetos con "id" genérico que tienen contexto de partido (fecha o minutos)
        for (JsonNode node : findObjects(data.json(), "id")) {
            if (!node.has("date") && !node.has("matchDate") && !node.has("minutes") && !node.has("minutesPlayed")) {
                continue;
            }
            Long partidoId = number(node, "id");
            if (partidoId == null) {
                continue;
            }
            AparicionBuilder builder = apariciones.computeIfAbsent(partidoId, AparicionBuilder::new);
            builder.merge(node);
        }

        // 2. Desde HTML si hay enlaces /matches/{id}/
        if (data.html() != null) {
            Pattern rowPattern = Pattern.compile("(?is)<tr[^>]*>(.*?)</tr>");
            Matcher rowMatcher = rowPattern.matcher(data.html());
            while (rowMatcher.find()) {
                String row = rowMatcher.group(1);
                Matcher matchMatcher = Pattern.compile("(?i)href=[\"']/matches/(\\d+)/(?:show|live)/[^\"'/?#]+[\"']")
                        .matcher(row);
                if (matchMatcher.find()) {
                    try {
                        Long partidoId = Long.valueOf(matchMatcher.group(1));
                        AparicionBuilder builder = apariciones.computeIfAbsent(partidoId, AparicionBuilder::new);
                        builder.mergeHtmlCell("date", htmlCell(row, "date"));
                        builder.mergeHtmlCell("minsPlayed", htmlCell(row, "minsPlayed"));
                        builder.mergeHtmlCell("goalTotal", htmlCell(row, "goalTotal"));
                        builder.mergeHtmlCell("assist", htmlCell(row, "assist"));
                        builder.mergeHtmlCell("yellowCard", htmlCell(row, "yellowCard"));
                        builder.mergeHtmlCell("redCard", htmlCell(row, "redCard"));
                        builder.mergeHtmlCell("shotsTotal", htmlCell(row, "shotsTotal"));
                        builder.mergeHtmlCell("passSuccess", htmlCell(row, "passSuccess"));
                        builder.mergeHtmlCell("duelAerialWon", htmlCell(row, "duelAerialWon"));
                        builder.mergeHtmlCell("rating", htmlCell(row, "rating"));
                        builder.mergeHtmlCell("opponent", htmlCell(row, "grid-abs"));
                    } catch (NumberFormatException ignored) {
                        LOGGER.debug("No se pudo interpretar un partido en el HTML de {}", ruta);
                    }
                }
            }
        }

        List<AparicionWhoscoredDto> resultado = apariciones.values().stream().map(AparicionBuilder::build).toList();
        if (resultado.isEmpty()) {
            LOGGER.warn("No se encontraron apariciones en la ruta '{}'. JSON roots: {}, HTML presente: {}",
                    ruta, data.json().size(), data.html() != null && !data.html().isBlank());
        }
        return resultado;
    }

    private PageData load(String path, String fase) {
        String url = configuration.getBaseUrl() + path;
        int maxRetries = Math.max(1, configuration.getMaxRetries() + 1);
        Exception lastException = null;

        for (int intento = 1; intento <= maxRetries; intento++) {
            try {
                if (intento > 1 && configuration.getBackoffMs() > 0) {
                    Thread.sleep((long) configuration.getBackoffMs() * (intento - 1));
                }
                return executeLoad(url, fase);
            } catch (Exception e) {
                lastException = e;
                LOGGER.warn("Intento {}/{} fallido al {} en {}: {}", intento, maxRetries, fase, url, e.getMessage());
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        throw new ScrapingException("No se pudo obtener datos de WhoScored tras " + maxRetries + " intentos", url, fase, lastException);
    }

    private PageData executeLoad(String url, String fase) {
        if (configuration.getPauseMs() > 0) {
            try {
                Thread.sleep(configuration.getPauseMs());
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }

        List<String> jsonResponses = new CopyOnWriteArrayList<>();
        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(configuration.isHeadless())
                    .setArgs(List.of(
                            "--disable-blink-features=AutomationControlled",
                            "--no-sandbox",
                            "--disable-dev-shm-usage",
                            "--disable-infobars"
                    ));

            try (Browser browser = playwright.chromium().launch(launchOptions)) {
                Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                        .setUserAgent(configuration.getUserAgent())
                        .setViewportSize(1920, 1080)
                        .setLocale("es-ES")
                        .setTimezoneId("America/Argentina/Buenos_Aires")
                        .setExtraHTTPHeaders(Map.of(
                                "Accept-Language", "es-ES,es;q=0.9,en;q=0.8",
                                "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8",
                                "sec-ch-ua", "\"Chromium\";v=\"124\", \"Google Chrome\";v=\"124\", \"Not-A.Brand\";v=\"99\"",
                                "sec-ch-ua-mobile", "?0",
                                "sec-ch-ua-platform", "\"Windows\""
                        ));

                try (BrowserContext context = browser.newContext(contextOptions)) {
                    context.addInitScript("""
                        Object.defineProperty(navigator, 'webdriver', {
                            get: () => undefined
                        });
                        window.chrome = {
                            runtime: {}
                        };
                        Object.defineProperty(navigator, 'languages', {
                            get: () => ['es-ES', 'es', 'en']
                        });
                    """);

                    try (Page page = context.newPage()) {
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

                        page.navigate(url, new Page.NavigateOptions()
                                .setTimeout(configuration.getNavigationTimeoutMs())
                                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

                        try {
                            page.waitForTimeout(3000);
                        } catch (Exception ignored) {}

                        String html = page.content();
                        List<JsonNode> json = new ArrayList<>();
                        for (String response : jsonResponses) {
                            try {
                                json.add(objectMapper.readTree(response));
                            } catch (Exception ignored) {
                                // Non-JSON responses are ignored; embedded JSON is tried below.
                            }
                        }

                        try {
                            Object ds = page.evaluate("() => typeof DataStore !== 'undefined' ? JSON.stringify(DataStore) : null");
                            if (ds != null && !ds.toString().isBlank() && !"null".equals(ds.toString())) {
                                json.add(objectMapper.readTree(ds.toString()));
                            }
                        } catch (Exception ignored) {}

                        try {
                            Object mcd = page.evaluate("() => typeof matchCentreData !== 'undefined' ? JSON.stringify(matchCentreData) : null");
                            if (mcd != null && !mcd.toString().isBlank() && !"null".equals(mcd.toString())) {
                                json.add(objectMapper.readTree(mcd.toString()));
                            }
                        } catch (Exception ignored) {}

                        // Extraer variables globales conocidas de WhoScored con datos de apariciones
                        for (String varName : List.of("playerMatchStats", "matchStatistics", "playerStats", "statsData")) {
                            try {
                                Object varData = page.evaluate("() => typeof " + varName + " !== 'undefined' ? JSON.stringify(" + varName + ") : null");
                                if (varData != null && !varData.toString().isBlank() && !"null".equals(varData.toString())) {
                                    json.add(objectMapper.readTree(varData.toString()));
                                }
                            } catch (Exception ignored) {}
                        }

                        for (String script : page.locator("script").allTextContents()) {
                            if (script.contains("\"playerId\"") || script.contains("\"teamId\"")
                                    || script.contains("\"leagueId\"") || script.contains("\"matchId\"")
                                    || script.contains("\"fixtureId\"") || script.contains("\"gameId\"")) {
                                extractJsonCandidatesFromScript(script, json);
                            }
                        }
                        return new PageData(json, html);
                    }

                }
            }
        }
    }

    private static String encodePathSegment(String value) {
        return URLEncoder.encode(URLDecoder.decode(value, StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                .replace("+", "%20");
    }

    private static String htmlCell(String row, String className) {
        Matcher matcher = Pattern.compile("(?is)<td[^>]*class=[\"'][^\"']*\\b"
                + Pattern.quote(className) + "\\b[^\"']*[\"'][^>]*>(.*?)</td>").matcher(row);
        if (!matcher.find()) return null;
        return decodeHtml(matcher.group(1).replaceAll("(?is)<[^>]+>", " ").replaceAll("\\s+", " ").trim());
    }

    private static String decodeHtml(String value) {
        return value.replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&#x27;", "'");
    }

    /**
     * Extrae todos los bloques JSON candidatos de un script inline.
     * A diferencia de la extracción anterior (que sólo tomaba el bloque mayor),
     * este método busca todos los bloques objeto de nivel raíz para no perder
     * estructuras de apariciones que vengan embebidas en asignaciones de variables.
     */
    private void extractJsonCandidatesFromScript(String script, List<JsonNode> target) {
        // Intentar extraer arrays o objetos JSON de asignaciones de variable JS:
        // e.g.: var playerMatchStats = [{...}, ...];
        java.util.regex.Pattern assignmentPattern = java.util.regex.Pattern.compile(
                "(?:var|let|const|window\\.)[\\w.]+=\\s*([\\[{])", java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher assignmentMatcher = assignmentPattern.matcher(script);
        while (assignmentMatcher.find()) {
            int valueStart = assignmentMatcher.start(1);
            char openChar = script.charAt(valueStart);
            char closeChar = openChar == '[' ? ']' : '}';
            int depth = 0;
            int end = -1;
            for (int i = valueStart; i < script.length(); i++) {
                char c = script.charAt(i);
                if (c == openChar) depth++;
                else if (c == closeChar) {
                    depth--;
                    if (depth == 0) { end = i + 1; break; }
                }
            }
            if (end > valueStart) {
                String candidate = script.substring(valueStart, end);
                try {
                    target.add(objectMapper.readTree(candidate));
                } catch (Exception ignored) {}
            }
        }

        // Fallback: extraer el bloque objeto más grande del script (comportamiento anterior)
        int start = script.indexOf('{');
        int end = script.lastIndexOf('}');
        if (start >= 0 && end > start) {
            String candidate = script.substring(start, end + 1);
            try {
                target.add(objectMapper.readTree(candidate));
            } catch (Exception ignored) {}
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
            node.forEach(child -> collect(child, result, keys));
        } else if (node.isArray()) {
            node.forEach(child -> collect(child, result, keys));
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
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.replace(".", "")
                .replace(",", "")
                .replaceAll("[^0-9-]", "");
        if (normalized.isEmpty() || "-".equals(normalized)) {
            return null;
        }
        try {
            return Long.valueOf(normalized);
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

        private void mergeHtmlCell(String key, String rawValue) {
            if (rawValue == null || rawValue.isBlank()) return;
            String value = rawValue.trim();
            if (!"-".equals(value)) {
                crudas.putIfAbsent(key, value);
            }
            switch (key) {
                case "date" -> fecha = firstDate(fecha, parseDate(value));
                case "minsPlayed" -> minutos = first(minutos, parseInteger(value));
                case "goalTotal" -> putIfPresent("goals", parseIntegerOrNull(value));
                case "assist" -> putIfPresent("assists", parseIntegerOrNull(value));
                case "yellowCard" -> putIfPresent("yellowCards", parseIntegerOrNull(value));
                case "redCard" -> putIfPresent("redCards", parseIntegerOrNull(value));
                case "shotsTotal" -> putIfPresent("shots", parseIntegerOrNull(value));
                case "passSuccess" -> putIfPresent("passSuccess", parseDecimalOrNull(value));
                case "rating" -> rating = first(rating, parseDecimal(value));
                case "opponent" -> {
                    String opponent = value.replaceAll("\\s+\\d+\\s*-\\s*\\d+\\s*$", "")
                            .replaceAll("\\s*\\([HA]\\)\\s*$", "").trim();
                    rivalNombre = first(rivalNombre, opponent);
                    localVisitante = first(localVisitante, value.matches(".*\\(H\\).*") ? "LOCAL" : "VISITANTE");
                }
                default -> {
                }
            }
        }

        private void putIfPresent(String key, Object value) {
            if (value != null) crudas.putIfAbsent(key, value);
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
        String value = text(node, names);
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.replace(".", "")
                .replace(",", "")
                .replaceAll("[^0-9-]", "");
        if (normalized.isEmpty() || "-".equals(normalized)) {
            return null;
        }
        try {
            return Integer.valueOf(normalized);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static Integer parseInteger(String value) {
        if (value == null || value.isBlank() || "-".equals(value)) return null;
        try {
            return Integer.valueOf(value.replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static Object parseIntegerOrNull(String value) {
        return parseInteger(value);
    }

    private static BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank() || "-".equals(value)) return null;
        try {
            return new BigDecimal(value.replace("%", "").trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static Object parseDecimalOrNull(String value) {
        return parseDecimal(value);
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank() || "-".equals(value)) return null;
        try {
            return LocalDate.parse(value, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (RuntimeException exception) {
            return dateFromIso(value);
        }
    }

    private static LocalDate dateFromIso(String value) {
        try {
            return LocalDate.parse(value.substring(0, 10));
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static BigDecimal decimal(JsonNode node, String... names) {
        String value = text(node, names);
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.replace("%", "")
                .replace("'", "")
                .replace("\u2019", "")
                .replace("\u2018", "");
        try {
            return new BigDecimal(normalized);
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
