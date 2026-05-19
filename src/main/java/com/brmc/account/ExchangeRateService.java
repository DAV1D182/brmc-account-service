package com.brmc.account;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servicio de aplicacion para obtener y aplicar la TRM USD/COP.
 *
 * <p>Consulta la fuente publica de Datos Abiertos Colombia cuando esta habilitada y mantiene cache
 * diario en memoria. Si la consulta remota falla o esta deshabilitada, usa la tasa fallback
 * configurada. Los pagos en COP siempre usan tasa 1.</p>
 */
@Service
class ExchangeRateService {

    private static final URI TRM_URI = URI.create(
            "https://www.datos.gov.co/resource/32sa-8pi3.json?$limit=1&$order=vigenciadesde%20DESC"
    );

    private final ObjectMapper objectMapper;
    private final BigDecimal fallbackUsdCopRate;
    private final boolean remoteEnabled;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private ExchangeRateResponse cachedUsdCopRate;
    private LocalDate cacheDate;

    /**
     * Crea el servicio de tasas de cambio.
     *
     * @param objectMapper lector JSON usado para procesar la respuesta remota.
     * @param fallbackUsdCopRate tasa fallback configurada para USD/COP.
     * @param remoteEnabled indica si se permite consultar la fuente remota.
     */
    ExchangeRateService(
            ObjectMapper objectMapper,
            @Value("${brmc.exchange.usd-cop-fallback:4000.00}") BigDecimal fallbackUsdCopRate,
            @Value("${brmc.exchange.remote-enabled:true}") boolean remoteEnabled
    ) {
        this.objectMapper = objectMapper;
        this.fallbackUsdCopRate = fallbackUsdCopRate;
        this.remoteEnabled = remoteEnabled;
    }

    /**
     * Obtiene la tasa USD a COP vigente para el dia.
     *
     * @return tasa remota cacheada o tasa fallback.
     */
    ExchangeRateResponse usdToCop() {
        if (LocalDate.now().equals(cacheDate) && cachedUsdCopRate != null) {
            return cachedUsdCopRate;
        }

        cachedUsdCopRate = fetchUsdCopRate()
                .orElseGet(() -> new ExchangeRateResponse(
                        Currency.USD,
                        Currency.COP,
                        fallbackUsdCopRate,
                        LocalDate.now(),
                        LocalDate.now(),
                        "TRM no disponible; valor de respaldo configurado"
                ));
        cacheDate = LocalDate.now();
        return cachedUsdCopRate;
    }

    /**
     * Convierte un monto a COP.
     *
     * @param amount monto original.
     * @param currency moneda original; COP si es nula.
     * @return el mismo monto para COP o monto multiplicado por TRM para USD.
     */
    BigDecimal convertToCop(BigDecimal amount, Currency currency) {
        if (currency == null || currency == Currency.COP) {
            return amount;
        }

        return amount.multiply(usdToCop().rate());
    }

    /**
     * Obtiene la tasa aplicable para una moneda de pago.
     *
     * @param currency moneda original; COP si es nula.
     * @return 1 para COP o TRM USD/COP para USD.
     */
    BigDecimal rateFor(Currency currency) {
        if (currency == null || currency == Currency.COP) {
            return BigDecimal.ONE;
        }

        return usdToCop().rate();
    }

    private Optional<ExchangeRateResponse> fetchUsdCopRate() {
        if (!remoteEnabled) {
            return Optional.empty();
        }

        try {
            var request = HttpRequest.newBuilder(TRM_URI)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return Optional.empty();
            }

            JsonNode rows = objectMapper.readTree(response.body());
            if (!rows.isArray() || rows.isEmpty()) {
                return Optional.empty();
            }

            JsonNode row = rows.get(0);
            var rate = new BigDecimal(row.path("valor").asText());
            var validFrom = parseDate(row.path("vigenciadesde").asText());
            var validTo = parseDate(row.path("vigenciahasta").asText());
            return Optional.of(new ExchangeRateResponse(
                    Currency.USD,
                    Currency.COP,
                    rate,
                    validFrom,
                    validTo,
                    "Datos Abiertos Colombia - TRM"
            ));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return LocalDate.now();
        }

        return LocalDateTime.parse(value).toLocalDate();
    }
}
