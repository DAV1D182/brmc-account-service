package com.brmc.account;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para consultar eventos de auditoria.
 *
 * <p>Permite filtrar eventos por cuenta y por tipo, reutilizando la logica de consulta de
 * {@link AccountService}.</p>
 */
@RestController
@RequestMapping("/api/events")
class SystemEventController {

    private final AccountService accountService;

    /**
     * Crea el controlador de eventos.
     *
     * @param accountService servicio que consulta eventos.
     */
    SystemEventController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Consulta eventos de sistema.
     *
     * @param accountId cuenta opcional.
     * @param type tipo de evento opcional.
     * @return eventos filtrados.
     */
    @GetMapping
    List<SystemEventResponse> getEvents(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) EventType type
    ) {
        return accountService.getEvents(accountId, type).stream()
                .map(SystemEventResponse::from)
                .toList();
    }
}
