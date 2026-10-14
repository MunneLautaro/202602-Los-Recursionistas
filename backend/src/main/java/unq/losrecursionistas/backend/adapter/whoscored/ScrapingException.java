package unq.losrecursionistas.backend.adapter.whoscored;

public class ScrapingException extends RuntimeException {
    private final String url;
    private final String fase;

    public ScrapingException(String message, String url, String fase, Throwable cause) {
        super(message, cause);
        this.url = url;
        this.fase = fase;
    }

    public ScrapingException(String message, String url, String fase) {
        this(message, url, fase, null);
    }

    public String getUrl() {
        return url;
    }

    public String getFase() {
        return fase;
    }
}
