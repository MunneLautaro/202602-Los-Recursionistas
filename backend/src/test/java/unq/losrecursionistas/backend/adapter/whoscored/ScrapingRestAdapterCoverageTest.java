package unq.losrecursionistas.backend.adapter.whoscored;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import unq.losrecursionistas.backend.adapter.whoscored.dto.AparicionWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.EquipoWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.JugadorWhoscoredDto;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ScrapingRestAdapterCoverageTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void cubreParseoDeEquiposJugadoresYConfiguracion() {
        ScrapingConfiguration configuration = configuration();
        ScrapingRestAdapter adapter = new ScrapingRestAdapter(configuration, mapper);

        assertThat(adapter.descubrirLigas()).isNotEmpty();
        assertThat(adapter.normalizedBaseUrl()).isEqualTo("https://example.test");
        assertThat(adapter.effectiveUserAgent()).startsWith("Mozilla/");
        assertThat(invoke(adapter, "cleanTeamName", "123 Arsenal FC", "fc-arsenal")).isEqualTo("Arsenal FC");
        assertThat(invoke(adapter, "cleanTeamName", "statistics", "fc-arsenal")).isEqualTo("arsenal");
        assertThat(invoke(adapter, "isValidTeamName", "Arsenal")).isEqualTo(true);
        assertThat(invoke(adapter, "isValidTeamName", "teammate")).isEqualTo(false);
        assertThat(invoke(adapter, "playerPath")).isEqualTo("/players/");

        configuration.setPlayerPath("/players");
        assertThat(invoke(adapter, "playerPath")).isEqualTo("/players/");
        configuration.setPlayerPath("");
        assertThatThrownBy(() -> invoke(adapter, "playerPath"))
                .hasRootCauseInstanceOf(ScrapingException.class);
    }

    @Test
    void cubreParseoDeJsonHtmlYFuncionesDeNormalizacion() throws Exception {
        ScrapingRestAdapter adapter = new ScrapingRestAdapter(configuration(), mapper);
        String script = "const data={\"matchId\":7,\"date\":\"2026-01-02\"};"
                + "window.stats=[{\"id\":8,\"minutes\":90}];";
        List<JsonNode> candidates = new ArrayList<>();
        invoke(adapter, "extractJsonCandidatesFromScript", script, candidates);
        invoke(adapter, "extractJsonCandidatesFromScript", "invalid {json", candidates);
        assertThat(candidates).isNotEmpty();

        String html = """
                <tr><td class="date">02-01-2026</td><td class="minsPlayed">90</td>
                <td class="goalTotal">2</td><td class="assist">1</td>
                <td class="yellowCard">1</td><td class="redCard">-</td>
                <td class="shotsTotal">4</td><td class="passSuccess">85%</td>
                <td class="duelAerialWon">3</td><td class="rating">7.5</td>
                <td class="grid-abs">Arsenal 2 - 1 Chelsea (H)</td></tr>
                """;
        Class<?> builderClass = Class.forName(
                "unq.losrecursionistas.backend.adapter.whoscored.ScrapingRestAdapter$AparicionBuilder");
        Constructor<?> constructor = builderClass.getDeclaredConstructor(Long.class);
        constructor.setAccessible(true);
        Object builder = constructor.newInstance(7L);
        invoke(builder, "mergeHtmlCell", "date", "02-01-2026");
        invoke(builder, "mergeHtmlCell", "minsPlayed", "90");
        invoke(builder, "mergeHtmlCell", "goalTotal", "2");
        invoke(builder, "mergeHtmlCell", "assist", "1");
        invoke(builder, "mergeHtmlCell", "yellowCard", "1");
        invoke(builder, "mergeHtmlCell", "redCard", "-");
        invoke(builder, "mergeHtmlCell", "shotsTotal", "4");
        invoke(builder, "mergeHtmlCell", "passSuccess", "85%");
        invoke(builder, "mergeHtmlCell", "rating", "7.5");
        invoke(builder, "mergeHtmlCell", "grid-abs", "Arsenal 2 - 1 Chelsea (H)");
        invoke(builder, "mergeHtmlCell", "unknown", "value");
        AparicionWhoscoredDto result = (AparicionWhoscoredDto) invoke(builder, "build");
        assertThat(result.partidoWhoscoredId()).isEqualTo(7L);
        assertThat(result.minutos()).isEqualTo(90);
        assertThat(result.statsCrudas()).containsEntry("goals", 2);

        assertThat(invokeStatic("encodePathSegment", "Lionel Messi")).isEqualTo("Lionel%20Messi");
        assertThat(invokeStatic("htmlCell", html, "date")).isEqualTo("02-01-2026");
        assertThat(invokeStatic("htmlCell", html, "missing")).isNull();
        assertThat(invokeStatic("decodeHtml", "&nbsp;&amp;&quot;&#39;&#x27;"))
                .isEqualTo(" &\"''");
    }

    @Test
    void cubreValidacionesYFallbacksDeApariciones() {
        ScrapingConfiguration configuration = configuration();
        ScrapingRestAdapter adapter = new ScrapingRestAdapter(configuration, mapper);

        assertThatThrownBy(() -> adapter.descubrirEquipos(0L)).isInstanceOf(ScrapingException.class);
        assertThatThrownBy(() -> adapter.descubrirJugadores(null)).isInstanceOf(ScrapingException.class);
        assertThatThrownBy(() -> adapter.obtenerAparicionesTemporada(10L, ""))
                .isInstanceOf(ScrapingException.class);
        configuration.setPlayerPath(null);
        assertThatThrownBy(() -> adapter.obtenerApariciones(10L)).isInstanceOf(ScrapingException.class);
        assertThat(ScrapingRestAdapter.looksBlocked(null)).isTrue();
        assertThat(ScrapingRestAdapter.looksBlocked("captcha")).isTrue();
        assertThat(ScrapingRestAdapter.looksBlocked("normal page")).isFalse();
    }

    @Test
    void cubreIntegracionConPageMockeada() {
        ScrapingRestAdapter adapter = new ScrapingRestAdapter(configuration(), mapper);
        Page page = mock(Page.class);
        Locator locator = mock(Locator.class);
        when(page.navigate(anyString(), any(Page.NavigateOptions.class))).thenReturn(null);
        when(page.content()).thenReturn("<html><body>ok</body></html>");
        when(page.locator("script")).thenReturn(locator);
        when(locator.allTextContents()).thenReturn(List.of("plain script"));
        when(page.evaluate(anyString())).thenReturn(null);

        Object result = invoke(adapter, "capturePage", page, "https://example.test", "test",
                new ArrayList<String>());
        assertThat(result).isNotNull();
        verify(page).setDefaultNavigationTimeout(30_000);
    }

    @Test
    void cubreOpcionesDeBrowserYRespuestasInvalidas() {
        ScrapingConfiguration configuration = configuration();
        configuration.setPauseMs(0);
        configuration.setMaxRetries(0);
        ScrapingRestAdapter adapter = new ScrapingRestAdapter(configuration, mapper);
        assertThat(invoke(adapter, "browserLaunchOptions")).isNotNull();
        invoke(adapter, "pauseBeforeLoad");
        assertThat(invoke(adapter, "parseResponses", List.of("not-json", "{\"id\":1}")))
                .asList().hasSize(1);

        Browser browser = mock(Browser.class);
        com.microsoft.playwright.BrowserContext browserContext = mock(com.microsoft.playwright.BrowserContext.class);
        when(browser.newContext(any())).thenReturn(browserContext);
        assertThat(invoke(adapter, "createBrowserContext", browser)).isNotNull();
        assertThatThrownBy(() -> adapter.descubrirEquipos(10L)).isInstanceOf(ScrapingException.class);
    }

    @Test
    void cubreRamasDeParseoJsonYHtml() throws Exception {
        ScrapingRestAdapter adapter = new ScrapingRestAdapter(configuration(), mapper);
        JsonNode root = mapper.readTree("""
                {"teamId":12,"name":"Arsenal","playerId":7,"playerName":"Player",
                 "matchId":99,"date":"2026-01-02","minutes":90,"positions":["Forward"],
                 "rating":"7.2","nested":{"id":8,"minutesPlayed":45}}
                """);
        List<JsonNode> roots = List.of(root);
        Map<Long, Object> teams = new LinkedHashMap<>();
        Map<Long, Object> appearances = new LinkedHashMap<>();
        invoke(adapter, "addTeamsFromJson", roots, 1L, teams);
        invoke(adapter, "addTeamsFromHtml",
                "<a href='/teams/12/show/arsenal'>123 Arsenal</a>"
                        + "<a href='/teams/x/show/bad'>statistics</a>", 1L, teams);
        invoke(adapter, "mergeExplicitJsonAppearances", roots, appearances);
        invoke(adapter, "mergeContextualJsonAppearances", roots, appearances);
        invoke(adapter, "mergeHtmlAppearances",
                "<tr><td class='date'>02-01-2026</td><td class='minsPlayed'>90</td>"
                        + "<td class='goalTotal'>1</td><td class='grid-abs'>Team 1 - Rival 0 (A)</td>"
                        + "<a href='/matches/99/show/test'>match</a></tr>",
                "/players/7/show", appearances);
        assertThat(teams).containsKey(12L);
        assertThat(appearances).containsKey(99L);

        assertThat(invokeStatic("text", root, new String[]{"missing", "name"})).isEqualTo("Arsenal");
        assertThat(invokeStatic("number", root, new String[]{"missing", "teamId"})).isEqualTo(12L);
        assertThat(invokeStatic("number", mapper.readTree("{\"id\":\"-\"}"), new String[]{"id"})).isNull();
        assertThat(invokeStatic("date", root, new String[]{"date"})).isEqualTo(LocalDate.of(2026, 1, 2));
        assertThat(invokeStatic("strings", root, new String[]{"positions"})).asList().contains("Forward");
        assertThat(invoke(adapter, "findObjects", roots, new String[]{"matchId"})).asList().isNotEmpty();
        assertThat(invoke(adapter, "findJsonBlockEnd", "[{\"id\":1}]", 0)).isEqualTo(10);
    }

    @Test
    void cubreRutasPublicasConPaginasFixture() throws Exception {
        ScrapingConfiguration configuration = configuration();
        JsonNode root = mapper.readTree("""
                {"leagueId":999,"name":"Fixture League","season":"2025/2026",
                 "teamId":12,"teamName":"Fixture Team","playerId":7,"playerName":"Fixture Player",
                 "date":"2026-01-02","matchId":99,"minutes":90,"rating":"7.2",
                 "tournamentName":"Fixture League","opponentName":"Rival"}
                """);
        FixtureAdapter adapter = new FixtureAdapter(configuration, mapper, List.of(root),
                "<a href='/teams/12/show/fixture-team'>Fixture Team</a>"
                        + "<a href='/players/7/show/fixture-player'>Fixture Player</a>");

        assertThat(adapter.descubrirLigas()).anyMatch(liga -> liga.idWhoscored() == 999L);
        assertThat(adapter.descubrirEquipos(12L)).anyMatch(equipo -> equipo.idWhoscored() == 12L);
        assertThat(adapter.descubrirJugadores(12L)).anyMatch(jugador -> jugador.idWhoscored() == 7L);
        assertThat(adapter.obtenerApariciones(7L)).anyMatch(aparicion -> aparicion.partidoWhoscoredId() == 99L);
        assertThat(adapter.obtenerAparicionesTemporada(7L, "fixture-player")).isNotEmpty();
    }

    private ScrapingConfiguration configuration() {
        ScrapingConfiguration configuration = new ScrapingConfiguration();
        configuration.setBaseUrl("https://example.test///");
        configuration.setPlayerPath("/players/");
        configuration.setPauseMs(0);
        configuration.setBackoffMs(0);
        configuration.setMaxRetries(0);
        return configuration;
    }

    private static Object invoke(Object target, String name, Object... args) {
        try {
            Method method = findMethod(target.getClass(), name, args);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static Object invokeStatic(String name, Object... args) {
        try {
            Method method = findMethod(ScrapingRestAdapter.class, name, args);
            method.setAccessible(true);
            return method.invoke(null, args);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static Method findMethod(Class<?> type, String name, Object[] args) {
        Class<?> current = type;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (!method.getName().equals(name) || method.getParameterCount() != args.length) {
                    continue;
                }
                boolean compatible = true;
                for (int index = 0; index < args.length; index++) {
                    if (args[index] != null && !wrap(method.getParameterTypes()[index]).isInstance(args[index])) {
                        compatible = false;
                    }
                }
                if (compatible) {
                    return method;
                }
            }
            current = current.getSuperclass();
        }
        throw new IllegalArgumentException("Método no encontrado: " + name);
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        return switch (type.getName()) {
            case "int" -> Integer.class;
            case "long" -> Long.class;
            case "boolean" -> Boolean.class;
            default -> type;
        };
    }

    private static final class FixtureAdapter extends ScrapingRestAdapter {
        private final List<JsonNode> json;
        private final String html;

        private FixtureAdapter(ScrapingConfiguration configuration, ObjectMapper mapper,
                               List<JsonNode> json, String html) {
            super(configuration, mapper);
            this.json = json;
            this.html = html;
        }

        @Override
        PageData load(String path, String fase) {
            return new PageData(json, html);
        }
    }
}
