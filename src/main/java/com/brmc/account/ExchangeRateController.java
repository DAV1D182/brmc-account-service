package com.brmc.account;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para consultar tasas de cambio usadas por pagos en USD.
 */
@RestController
@RequestMapping("/api/exchange-rates")
class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    /**
     * Crea el controlador de tasas de cambio.
     *
     * @param exchangeRateService servicio de TRM.
     */
    ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    /**
     * Consulta la tasa USD/COP vigente o fallback.
     *
     * @return tasa usada por el sistema para convertir pagos USD a COP.
     */
    @GetMapping("/usd-cop")
    ExchangeRateResponse usdToCop() {
        return exchangeRateService.usdToCop();
    }
}
