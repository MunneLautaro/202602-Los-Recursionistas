package unq.losrecursionistas.backend.adapter.whoscored;

import org.junit.jupiter.api.Test;
import unq.losrecursionistas.backend.adapter.whoscored.dto.JugadorWhoscoredDto;
import unq.losrecursionistas.backend.adapter.whoscored.dto.LigaWhoscoredDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScrapingRestAdapterTest {

    @Test
    void replacesNonBrowserUserAgentToAvoidBeingBlockedByWhoScored() {
        ScrapingConfiguration configuration = new ScrapingConfiguration();
        configuration.setUserAgent("Los-Recursionistas/1.0");

        ScrapingRestAdapter adapter = new ScrapingRestAdapter(configuration, null);

        assertThat(adapter.effectiveUserAgent()).startsWith("Mozilla/");
    }

    @Test
    void keepsConfiguredBrowserUserAgent() {
        ScrapingConfiguration configuration = new ScrapingConfiguration();
        configuration.setUserAgent("Mozilla/5.0 custom-browser");

        ScrapingRestAdapter adapter = new ScrapingRestAdapter(configuration, null);

        assertThat(adapter.effectiveUserAgent()).isEqualTo("Mozilla/5.0 custom-browser");
    }

    @Test
    void extractsPlayerIdFromCurrentWhoScoredHtmlLinks() {
        String html = """
        <a class="player-link"
           href="https://www.whoscored.com/Players/123/Show/test-player">
            <span>Test Player</span>
        </a>
        <a href="/players/456/profile/second-player">Second Player</a>
        """;

        assertThat(ScrapingRestAdapter.parsePlayerLinks(html, 99L))
        .extracting(JugadorWhoscoredDto::idWhoscored)
        .containsExactly(123L, 456L);
    }

    @Test
    void removesTrailingSlashFromBaseUrlBeforeBuildingRequest() {
        ScrapingConfiguration configuration = new ScrapingConfiguration();
        configuration.setBaseUrl("https://www.whoscored.com///");

        ScrapingRestAdapter adapter = new ScrapingRestAdapter(configuration, null);

        assertThat(adapter.normalizedBaseUrl()).isEqualTo("https://www.whoscored.com");
    }

    @Test
    void detectsCloudflareAndCaptchaPagesInsteadOfTreatingThemAsEmptyData() {
        assertThat(ScrapingRestAdapter.looksBlocked(
                "<html><title>Just a moment...</title><div class=\"cf-chl-widget\"></div></html>"))
                .isTrue();
        assertThat(ScrapingRestAdapter.looksBlocked(
                "<html><body><a href=\"/players/123/show/test-player\">Test Player</a></body></html>"))
                .isFalse();
    }

    @Test
    void devuelveLigasConfiguradasCuandoNoPuedeCargarIndice() {
        ScrapingRestAdapter adapter = new ScrapingRestAdapter(new ScrapingConfiguration(), null);

        assertThat(adapter.descubrirLigas()).extracting(LigaWhoscoredDto::idWhoscored)
                .contains(252L, 206L, 81L, 108L, 74L);
    }

    @Test
    void validaIdsAntesDeAccederAlSitio() {
        ScrapingRestAdapter adapter = new ScrapingRestAdapter(new ScrapingConfiguration(), null);

        assertThatThrownBy(() -> adapter.descubrirEquipos(null)).isInstanceOf(ScrapingException.class);
        assertThatThrownBy(() -> adapter.descubrirJugadores(null)).isInstanceOf(ScrapingException.class);
        assertThatThrownBy(() -> adapter.obtenerApariciones(null)).isInstanceOf(ScrapingException.class);
        assertThatThrownBy(() -> adapter.obtenerAparicionesTemporada(null, "slug"))
                .isInstanceOf(ScrapingException.class);
    }
}
