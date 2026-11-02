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
    private static final String TEMPORADA_ACTUAL = "2025/2026";
    private static final String FIELD_GOAL_TOTAL = "goalTotal";
    private static final String FIELD_ASSIST = "assist";
    private static final String FIELD_DATE = "date";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_PLAYER_ID = "playerId";
    private static final String FIELD_SLUG = "slug";
    private static final String FIELD_URL = "url";
    private static final String FIELD_YELLOW_CARD = "yellowCard";
    private static final String FIELD_RED_CARD = "redCard";
    private static final String FIELD_SHOTS_TOTAL = "shotsTotal";
    private static final String FIELD_PASS_SUCCESS = "passSuccess";
    private static final String FIELD_RATING = "rating";
    private static final String FIELD_MINS_PLAYED = "minsPlayed";
    private static final String FIELD_DUEL_AERIAL_WON = "duelAerialWon";
    private static final String FIELD_OPPONENT = "opponent";
    private static final String FIELD_TEAM_ID = "teamId";
    private static final String FIELD_TEAM_NAME = "teamName";
    private static final String PLAYER_PATH = "/players/";
    private static final String NON_NUMERIC_CHARACTERS = "[^0-9-]";
    static final String DEFAULT_BROWSER_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    private static final Map<Long, String> TOURNAMENT_PATHS = Map.of(
            252L, "/Regions/252/Tournaments/2/England-Premier-League",
            206L, "/Regions/206/Tournaments/4/Spain-LaLiga",
            81L, "/Regions/81/Tournaments/3/Germany-Bundesliga",
            108L, "/Regions/108/Tournaments/5/Italy-Serie-A",
            74L, "/Regions/74/Tournaments/22/France-Ligue-1"
    );

    private static final List<LigaWhoscoredDto> LIGAS_TOP = List.of(
            new LigaWhoscoredDto(252L, "Premier League", "/Regions/252/Tournaments/2/England-Premier-League", TEMPORADA_ACTUAL),
            new LigaWhoscoredDto(206L, "LaLiga", "/Regions/206/Tournaments/4/Spain-LaLiga", TEMPORADA_ACTUAL),
            new LigaWhoscoredDto(81L, "Bundesliga", "/Regions/81/Tournaments/3/Germany-Bundesliga", TEMPORADA_ACTUAL),
            new LigaWhoscoredDto(108L, "Serie A", "/Regions/108/Tournaments/5/Italy-Serie-A", TEMPORADA_ACTUAL),
            new LigaWhoscoredDto(74L, "Ligue 1", "/Regions/74/Tournaments/22/France-Ligue-1", TEMPORADA_ACTUAL)
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
                String nombre = text(node, FIELD_NAME, "leagueName");
                if (id != null && nombre != null) {
                    result.add(new LigaWhoscoredDto(id, nombre, text(node, FIELD_SLUG, FIELD_URL), text(node, "season")));
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
        addTeamsFromJson(data.json(), ligaWhoscoredId, result);
        addTeamsFromHtml(data.html(), ligaWhoscoredId, result);
        return new ArrayList<>(result.values());
    }

    private void addTeamsFromJson(List<JsonNode> roots, Long ligaWhoscoredId,
                                  Map<Long, EquipoWhoscoredDto> result) {
        for (JsonNode node : findObjects(roots, FIELD_TEAM_ID, "team")) {
            Long id = number(node, FIELD_TEAM_ID, "id");
            String nombre = text(node, FIELD_NAME, FIELD_TEAM_NAME);
            if (id != null && nombre != null) {
                result.put(id, new EquipoWhoscoredDto(id, nombre, text(node, FIELD_SLUG, FIELD_URL), ligaWhoscoredId));
            }
        }
    }

    private void addTeamsFromHtml(String html, Long ligaWhoscoredId,
                                  Map<Long, EquipoWhoscoredDto> result) {
        if (html == null) {
            return;
        }
        Pattern pattern = Pattern.compile(
                "(?i)href=[\"']/teams/(\\d+)/(?:show/)?([^\"'/?#]+)[\"'][^>]*>(.*?)</a>",
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            addTeamFromHtmlMatch(matcher, ligaWhoscoredId, result);
        }
    }

    private void addTeamFromHtmlMatch(Matcher matcher, Long ligaWhoscoredId,
                                      Map<Long, EquipoWhoscoredDto> result) {
        try {
            Long id = Long.valueOf(matcher.group(1));
            String slugPart = matcher.group(2);
            String nombre = cleanTeamName(matcher.group(3), slugPart);
            if (isValidTeamName(nombre)) {
                result.putIfAbsent(id, new EquipoWhoscoredDto(
                        id, nombre, "/teams/" + id + "/show/" + slugPart, ligaWhoscoredId));
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private String cleanTeamName(String rawHtml, String slugPart) {
        String nombre = rawHtml.replaceAll("<[^>]*>", "").trim().replaceAll("^\\d+\\s*", "").trim();
        if (nombre.isBlank() || nombre.equalsIgnoreCase("teammate")
                || nombre.toLowerCase().contains("statistics")) {
            return slugPart.replaceAll("^[a-z]+-", "").replace("-", " ").trim();
        }
        return nombre;
    }

    private boolean isValidTeamName(String nombre) {
        return !nombre.isBlank() && !nombre.equalsIgnoreCase("teammate")
                && !nombre.toLowerCase().contains("statistics");
    }

    @Override
    public List<JugadorWhoscoredDto> descubrirJugadores(Long equipoWhoscoredId) {
        requireId(equipoWhoscoredId, "equipo");
        PageData data = load("/teams/" + equipoWhoscoredId + "/show", "descubrir jugadores");
        Map<Long, JugadorWhoscoredDto> result = new LinkedHashMap<>();

        // 1. Parsear objetos JSON si están presentes
        for (JsonNode node : findObjects(data.json(), FIELD_PLAYER_ID, "playerID", "player_id", "playerName")) {
            Long id = number(node, FIELD_PLAYER_ID, "playerID", "player_id", "id");
            String nombre = text(node, FIELD_NAME, "playerName", "fullName");
            if (id != null && nombre != null) {
                result.put(id, new JugadorWhoscoredDto(id, nombre, text(node, FIELD_SLUG, FIELD_URL),
                        date(node, "dateOfBirth", "birthDate"), text(node, "nationality", "countryName"),
                        equipoWhoscoredId, text(node, FIELD_TEAM_NAME), strings(node, "positions", "position")));
            }
        }

        // 2. Parsear enlaces HTML del DOM (/players/{id}/show/{slug})
        if (data.html() != null) {
            for (JugadorWhoscoredDto jugador : parsePlayerLinks(data.html(), equipoWhoscoredId)) {
                result.putIfAbsent(jugador.idWhoscored(), jugador);
            }
        }

        return new ArrayList<>(result.values());
    }

    @Override
    public List<AparicionWhoscoredDto> obtenerApariciones(Long jugadorWhoscoredId) {
        requireId(jugadorWhoscoredId, "jugador");
        List<AparicionWhoscoredDto> apariciones = obtenerAparicionesDesdeRuta(PLAYER_PATH + jugadorWhoscoredId + "/show", "obtener apariciones");
        if (apariciones.isEmpty()) {
            try {
                apariciones = obtenerAparicionesDesdeRuta(PLAYER_PATH + jugadorWhoscoredId + "/matchstatistics", "obtener apariciones matchstatistics");
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
                PLAYER_PATH + jugadorWhoscoredId + "/matchstatistics/" + encodePathSegment(slug),
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
            if (!node.has(FIELD_DATE) && !node.has("matchDate") && !node.has("minutes") && !node.has("minutesPlayed")) {
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
                        mergeHtmlAppearanceCells(builder, row);
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

    private void mergeHtmlAppearanceCells(AparicionBuilder builder, String row) {
        builder.mergeHtmlCell(FIELD_DATE, htmlCell(row, FIELD_DATE));
        builder.mergeHtmlCell(FIELD_MINS_PLAYED, htmlCell(row, FIELD_MINS_PLAYED));
        builder.mergeHtmlCell(FIELD_GOAL_TOTAL, htmlCell(row, FIELD_GOAL_TOTAL));
        builder.mergeHtmlCell(FIELD_ASSIST, htmlCell(row, FIELD_ASSIST));
        builder.mergeHtmlCell(FIELD_YELLOW_CARD, htmlCell(row, FIELD_YELLOW_CARD));
        builder.mergeHtmlCell(FIELD_RED_CARD, htmlCell(row, FIELD_RED_CARD));
        builder.mergeHtmlCell(FIELD_SHOTS_TOTAL, htmlCell(row, FIELD_SHOTS_TOTAL));
        builder.mergeHtmlCell(FIELD_PASS_SUCCESS, htmlCell(row, FIELD_PASS_SUCCESS));
        builder.mergeHtmlCell(FIELD_DUEL_AERIAL_WON, htmlCell(row, FIELD_DUEL_AERIAL_WON));
        builder.mergeHtmlCell(FIELD_RATING, htmlCell(row, FIELD_RATING));
        builder.mergeHtmlCell(FIELD_OPPONENT, htmlCell(row, "grid-abs"));
    }

    private PageData load(String path, String fase) {
        String url = normalizedBaseUrl() + path;
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
        pauseBeforeLoad();
        List<String> jsonResponses = new CopyOnWriteArrayList<>();
        try (Playwright playwright = Playwright.create()) {
            try (Browser browser = playwright.chromium().launch(browserLaunchOptions());
                 BrowserContext context = createBrowserContext(browser)) {
                context.addInitScript(browserInitScript());
                try (Page page = context.newPage()) {
                    return capturePage(page, url, fase, jsonResponses);
                }
            }
        }
    }

    private void pauseBeforeLoad() {
        if (configuration.getPauseMs() <= 0) {
            return;
        }
        try {
            Thread.sleep(configuration.getPauseMs());
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private BrowserType.LaunchOptions browserLaunchOptions() {
        return new BrowserType.LaunchOptions().setHeadless(configuration.isHeadless()).setArgs(List.of(
                "--disable-blink-features=AutomationControlled", "--no-sandbox",
                "--disable-dev-shm-usage", "--disable-infobars"));
    }

    private BrowserContext createBrowserContext(Browser browser) {
        Browser.NewContextOptions options = new Browser.NewContextOptions()
                .setUserAgent(effectiveUserAgent()).setViewportSize(1920, 1080)
                .setLocale("es-ES").setTimezoneId("America/Argentina/Buenos_Aires")
                .setExtraHTTPHeaders(Map.of(
                        "Accept-Language", "es-ES,es;q=0.9,en;q=0.8",
                        "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8",
                        "sec-ch-ua", "\"Chromium\";v=\"124\", \"Google Chrome\";v=\"124\", \"Not-A.Brand\";v=\"99\"",
                        "sec-ch-ua-mobile", "?0", "sec-ch-ua-platform", "\"Windows\""));
        return browser.newContext(options);
    }

    private String browserInitScript() {
        return """
                Object.defineProperty(navigator, 'webdriver', { get: () => undefined });
                window.chrome = { runtime: {} };
                Object.defineProperty(navigator, 'languages', { get: () => ['es-ES', 'es', 'en'] });
                """;
    }

    private PageData capturePage(Page page, String url, String fase, List<String> jsonResponses) {
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
        var navigationResponse = page.navigate(url, new Page.NavigateOptions()
                .setTimeout(configuration.getNavigationTimeoutMs())
                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        if (navigationResponse != null && navigationResponse.status() >= 400) {
            throw new ScrapingException("WhoScored respondió HTTP " + navigationResponse.status(), url, fase);
        }
        try {
            page.waitForTimeout(3000);
        } catch (Exception ignored) {
        }
        String html = page.content();
        if (looksBlocked(html)) {
            throw new ScrapingException("WhoScored devolvió una página de bloqueo o verificación", url, fase);
        }
        List<JsonNode> json = parseResponses(jsonResponses);
        addKnownPageData(page, json);
        return new PageData(json, html);
    }

    private List<JsonNode> parseResponses(List<String> responses) {
        List<JsonNode> json = new ArrayList<>();
        for (String response : responses) {
            try {
                json.add(objectMapper.readTree(response));
            } catch (Exception ignored) {
                // Non-JSON responses are ignored; embedded JSON is tried below.
            }
        }
        return json;
    }

    private void addKnownPageData(Page page, List<JsonNode> json) {
        addPageVariable(page, "DataStore", json);
        addPageVariable(page, "matchCentreData", json);
        for (String varName : List.of("playerMatchStats", "matchStatistics", "playerStats", "statsData")) {
            addPageVariable(page, varName, json);
        }
        for (String script : page.locator("script").allTextContents()) {
            if (script.contains("\"playerId\"") || script.contains("\"teamId\"")
                    || script.contains("\"leagueId\"") || script.contains("\"matchId\"")
                    || script.contains("\"fixtureId\"") || script.contains("\"gameId\"")) {
                extractJsonCandidatesFromScript(script, json);
            }
        }
    }

    private void addPageVariable(Page page, String variableName, List<JsonNode> json) {
        try {
            Object value = page.evaluate("() => typeof " + variableName
                    + " !== 'undefined' ? JSON.stringify(" + variableName + ") : null");
            if (value != null && !value.toString().isBlank() && !"null".equals(value.toString())) {
                json.add(objectMapper.readTree(value.toString()));
            }
        } catch (Exception ignored) {
        }
    }

    String effectiveUserAgent() {
        String configured = configuration.getUserAgent();
        return configured != null && configured.startsWith("Mozilla/")
                ? configured
                : DEFAULT_BROWSER_USER_AGENT;
    }

    String normalizedBaseUrl() {
        String baseUrl = configuration.getBaseUrl();
        if (baseUrl == null) {
            return "";
        }
        int end = baseUrl.length();
        while (end > 0 && baseUrl.charAt(end - 1) == '/') {
            end--;
        }
        return baseUrl.substring(0, end);
    }

    static List<JugadorWhoscoredDto> parsePlayerLinks(String html, Long equipoWhoscoredId) {
        if (html == null || html.isBlank()) {
            return List.of();
        }
        Map<Long, JugadorWhoscoredDto> result = new LinkedHashMap<>();
        Pattern pattern = Pattern.compile(
                "(?is)href=[\"'](?:https?://[^/]+)?/players?/(\\d+)"
                        + "(?:/(?:show|profile))?/([^\"'/?#]+)[\"'][^>]*>(.*?)</a>");
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            try {
                Long id = Long.valueOf(matcher.group(1));
                String slugPart = matcher.group(2);
                String rawText = decodeHtml(matcher.group(3)
                        .replaceAll("<[^>]*>", " ")
                        .replaceAll("\\s+", " ").trim());
                String nombre = rawText.replaceAll("^\\d+\\s*", "").trim();
                if (nombre.isBlank()) {
                    nombre = slugPart.replace("-", " ").trim();
                }
                if (!nombre.isBlank()) {
                    result.putIfAbsent(id, new JugadorWhoscoredDto(id, nombre,
                            PLAYER_PATH + id + "/show/" + slugPart,
                            null, null, equipoWhoscoredId, null, List.of()));
                }
            } catch (NumberFormatException ignored) {
                LOGGER.debug("No se pudo interpretar un ID de jugador en el HTML de WhoScored");
            }
        }
        return new ArrayList<>(result.values());
    }

    static boolean looksBlocked(String html) {
        if (html == null || html.isBlank()) {
            return true;
        }
        String content = html.toLowerCase();
        return content.contains("cf-chl-") || content.contains("just a moment")
                || content.contains("checking your browser") || content.contains("access denied")
                || content.contains("captcha");
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
        addAssignedJsonCandidates(script, target);
        int start = script.indexOf('{');
        int end = script.lastIndexOf('}');
        addJsonCandidate(script, start, end + 1, target);
    }

    private void addAssignedJsonCandidates(String script, List<JsonNode> target) {
        Pattern assignmentPattern = Pattern.compile(
                "(?:var|let|const|window\\.)[\\w.]+=\\s*([\\[{])", Pattern.DOTALL);
        Matcher assignmentMatcher = assignmentPattern.matcher(script);
        while (assignmentMatcher.find()) {
            int valueStart = assignmentMatcher.start(1);
            int end = findJsonBlockEnd(script, valueStart);
            addJsonCandidate(script, valueStart, end, target);
        }
    }

    private int findJsonBlockEnd(String script, int valueStart) {
        char openChar = script.charAt(valueStart);
        char closeChar = openChar == '[' ? ']' : '}';
        int depth = 0;
        for (int index = valueStart; index < script.length(); index++) {
            char current = script.charAt(index);
            if (current == openChar) {
                depth++;
            } else if (current == closeChar && --depth == 0) {
                return index + 1;
            }
        }
        return -1;
    }

    private void addJsonCandidate(String script, int start, int end, List<JsonNode> target) {
        if (start < 0 || end <= start) {
            return;
        }
        try {
            target.add(objectMapper.readTree(script.substring(start, end)));
        } catch (Exception ignored) {
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
                .replaceAll(NON_NUMERIC_CHARACTERS, "");
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
                value.forEach(item -> result.add(item.isValueNode() ? item.asText() : text(item, FIELD_NAME)));
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
            fecha = firstDate(fecha, date(node, FIELD_DATE, "matchDate", "gameDate"));
            torneo = first(torneo, text(node, "tournamentName", "competitionName", "tournament", "competition"));
            torneoId = first(torneoId, number(node, "tournamentId", "competitionId"));
            equipoId = first(equipoId, number(node, FIELD_TEAM_ID, "playerTeamId"));
            equipoNombre = first(equipoNombre, text(node, FIELD_TEAM_NAME, "playerTeam"));
            rivalId = first(rivalId, number(node, "opponentId", "rivalId"));
            rivalNombre = first(rivalNombre, text(node, "opponentName", "rivalName"));
            localVisitante = first(localVisitante, text(node, "venue", "homeAway", "location"));
            minutos = first(minutos, integer(node, "minutes", "minutesPlayed", "mins"));
            rating = first(rating, decimal(node, FIELD_RATING, "playerRating"));
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
                case FIELD_DATE -> fecha = firstDate(fecha, parseDate(value));
                case FIELD_MINS_PLAYED -> minutos = first(minutos, parseInteger(value));
                case FIELD_GOAL_TOTAL -> putIfPresent("goals", parseIntegerOrNull(value));
                case FIELD_ASSIST -> putIfPresent("assists", parseIntegerOrNull(value));
                case FIELD_YELLOW_CARD -> putIfPresent("yellowCards", parseIntegerOrNull(value));
                case FIELD_RED_CARD -> putIfPresent("redCards", parseIntegerOrNull(value));
                case FIELD_SHOTS_TOTAL -> putIfPresent("shots", parseIntegerOrNull(value));
                case FIELD_PASS_SUCCESS -> putIfPresent(FIELD_PASS_SUCCESS, parseDecimalOrNull(value));
                case FIELD_RATING -> rating = first(rating, parseDecimal(value));
                case FIELD_OPPONENT -> {
                    String opponent = removeTrailingVenue(removeTrailingScore(value));
                    rivalNombre = first(rivalNombre, opponent);
                    localVisitante = first(localVisitante, value.contains("(H)") ? "LOCAL" : "VISITANTE");
                }
                default -> {
                }
            }
        }

        private String removeTrailingScore(String value) {
            int end = value.length();
            while (end > 0 && Character.isWhitespace(value.charAt(end - 1))) {
                end--;
            }
            int cursor = end - 1;
            while (cursor >= 0 && Character.isDigit(value.charAt(cursor))) {
                cursor--;
            }
            if (cursor == end - 1) {
                return value;
            }
            while (cursor >= 0 && Character.isWhitespace(value.charAt(cursor))) {
                cursor--;
            }
            if (cursor < 0 || value.charAt(cursor) != '-') {
                return value;
            }
            cursor--;
            while (cursor >= 0 && Character.isWhitespace(value.charAt(cursor))) {
                cursor--;
            }
            int firstScoreEnd = cursor + 1;
            while (cursor >= 0 && Character.isDigit(value.charAt(cursor))) {
                cursor--;
            }
            if (cursor == firstScoreEnd - 1) {
                return value;
            }
            return value.substring(0, cursor + 1);
        }

        private String removeTrailingVenue(String value) {
            String trimmed = value.trim();
            if (trimmed.endsWith("(H)") || trimmed.endsWith("(A)")) {
                return trimmed.substring(0, trimmed.length() - 3).trim();
            }
            return trimmed;
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
                .replaceAll(NON_NUMERIC_CHARACTERS, "");
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
            return Integer.valueOf(value.replaceAll(NON_NUMERIC_CHARACTERS, ""));
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
