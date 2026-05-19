package com.brmc.account;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de respuesta para tasas de cambio usadas por pagos en USD.
 *
 * @param fromCurrency moneda origen.
 * @param toCurrency moneda destino.
 * @param rate tasa de conversion.
 * @param validFrom fecha inicial de vigencia.
 * @param validTo fecha final de vigencia.
 * @param source fuente de la tasa o descripcion del fallback.
 */
record ExchangeRateResponse(
        Currency fromCurrency,
        Currency toCurrency,
        BigDecimal rate,
        LocalDate validFrom,
        LocalDate validTo,
        String source
) {
}
