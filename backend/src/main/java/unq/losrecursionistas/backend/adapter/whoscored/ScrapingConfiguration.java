package unq.losrecursionistas.backend.adapter.whoscored;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "whoscored.scraping")
public class ScrapingConfiguration {
    private boolean headless = true;
    private int navigationTimeoutMs = 30_000;
    private int requestTimeoutMs = 15_000;
    private String userAgent = "Los-Recursionistas/1.0";
    private String baseUrl = "https://www.whoscored.com";
    private int pauseMs = 1_000;
    private int rateLimitPerMinute = 30;
    private int maxRetries = 2;
    private int backoffMs = 2_000;
    private LocalDate temporadaInicio;
    private LocalDate temporadaFin;
    private List<Long> ligasPermitidas = new ArrayList<>();
    private boolean startupEnabled = true;
}
