package com.brmc.account;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para consultas especializadas de pagos.
 *
 * <p>Complementa las operaciones de cuenta con consultas globales de pagos no asignados, utiles
 * para conciliacion y para simular el concepto BRM de Unallocated Payments.</p>
 */
@RestController
@RequestMapping("/api/payments")
class PaymentRestController {

    private final AccountService accountService;

    /**
     * Crea el controlador de pagos.
     *
     * @param accountService servicio de cuentas y pagos.
     */
    PaymentRestController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Lista todos los pagos que no han sido asignados a bills o items.
     *
     * @return pagos con estado UNALLOCATED.
     */
    @GetMapping("/unallocated")
    List<PaymentRecordResponse> getUnallocatedPayments() {
        return accountService.getUnallocatedPayments().stream()
                .map(PaymentRecordResponse::from)
                .toList();
    }
}
