package com.brmc.account;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST del modulo de disputas y settlements.
 *
 * <p>Permite crear disputas, aprobarlas, rechazarlas y registrar settlements. La creacion de un
 * settlement delega en {@link AccountService} el cierre automatico de la disputa como SETTLED.</p>
 */
@RestController
@RequestMapping("/api/disputes")
class DisputeController {

    private final AccountService accountService;

    /**
     * Crea el controlador de disputas.
     *
     * @param accountService servicio de cuentas que administra disputas.
     */
    DisputeController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Consulta disputas con filtros opcionales.
     *
     * @param accountId cuenta opcional.
     * @param status estado opcional.
     * @return disputas encontradas.
     */
    @GetMapping
    List<DisputeResponse> getDisputes(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) DisputeStatus status
    ) {
        return accountService.getDisputes(accountId, status).stream()
                .map(DisputeResponse::from)
                .toList();
    }

    /**
     * Crea una disputa pendiente.
     *
     * @param request datos validados de la disputa.
     * @return disputa creada.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    DisputeResponse createDispute(@Valid @RequestBody CreateDisputeRequest request) {
        return DisputeResponse.from(accountService.createDispute(
                request.accountId(),
                request.amount(),
                request.reason()
        ));
    }

    /**
     * Aprueba una disputa pendiente.
     *
     * @param disputeId disputa a aprobar.
     * @param request nota opcional de resolucion.
     * @return disputa aprobada.
     */
    @PostMapping("/{disputeId}/approve")
    DisputeResponse approveDispute(
            @PathVariable String disputeId,
            @Valid @RequestBody ResolveDisputeRequest request
    ) {
        return DisputeResponse.from(accountService.approveDispute(disputeId, request.resolutionNote()));
    }

    /**
     * Rechaza una disputa pendiente.
     *
     * @param disputeId disputa a rechazar.
     * @param request nota opcional de resolucion.
     * @return disputa rechazada.
     */
    @PostMapping("/{disputeId}/reject")
    DisputeResponse rejectDispute(
            @PathVariable String disputeId,
            @Valid @RequestBody ResolveDisputeRequest request
    ) {
        return DisputeResponse.from(accountService.rejectDispute(disputeId, request.resolutionNote()));
    }

    /**
     * Lista settlements de una disputa.
     *
     * @param disputeId disputa consultada.
     * @return settlements registrados.
     */
    @GetMapping("/{disputeId}/settlements")
    List<DisputeSettlementResponse> getSettlements(@PathVariable String disputeId) {
        return accountService.getDisputeSettlements(disputeId).stream()
                .map(DisputeSettlementResponse::from)
                .toList();
    }

    /**
     * Crea un settlement para una disputa pendiente.
     *
     * @param disputeId disputa a cerrar.
     * @param request monto y nota del acuerdo.
     * @return settlement creado.
     */
    @PostMapping("/{disputeId}/settlements")
    @ResponseStatus(HttpStatus.CREATED)
    DisputeSettlementResponse createSettlement(
            @PathVariable String disputeId,
            @Valid @RequestBody CreateSettlementRequest request
    ) {
        return DisputeSettlementResponse.from(accountService.createDisputeSettlement(
                disputeId,
                request.amount(),
                request.note()
        ));
    }

    /**
     * Solicitud de creacion de disputa.
     *
     * @param accountId cuenta asociada.
     * @param amount monto positivo en COP.
     * @param reason motivo obligatorio.
     */
    record CreateDisputeRequest(
            @NotBlank(message = "accountId es obligatorio")
            String accountId,

            @NotNull(message = "amount es obligatorio")
            @DecimalMin(value = "0.01", message = "amount debe ser mayor a cero")
            BigDecimal amount,

            @NotBlank(message = "reason es obligatorio")
            @Size(max = 240, message = "reason no puede superar 240 caracteres")
            String reason
    ) {
    }

    /**
     * Solicitud de resolucion de disputa.
     *
     * @param resolutionNote nota opcional de resolucion.
     */
    record ResolveDisputeRequest(
            @Size(max = 240, message = "resolutionNote no puede superar 240 caracteres")
            String resolutionNote
    ) {
    }

    /**
     * Solicitud de creacion de settlement.
     *
     * @param amount monto positivo del acuerdo.
     * @param note nota obligatoria del acuerdo.
     */
    record CreateSettlementRequest(
            @NotNull(message = "amount es obligatorio")
            @DecimalMin(value = "0.01", message = "amount debe ser mayor a cero")
            BigDecimal amount,

            @NotBlank(message = "note es obligatorio")
            @Size(max = 240, message = "note no puede superar 240 caracteres")
            String note
    ) {
    }
}
