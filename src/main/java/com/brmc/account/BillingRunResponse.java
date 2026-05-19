package com.brmc.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para una corrida de billing.
 *
 * @param id identificador interno.
 * @param runCode codigo funcional.
 * @param runType alcance de la corrida.
 * @param status estado final o actual.
 * @param virtualTime fecha virtual usada.
 * @param startedAt fecha real de inicio.
 * @param finishedAt fecha real de cierre, si existe.
 * @param accountsProcessed cuentas consideradas.
 * @param chargesCreated cargos generados.
 * @param totalAmount suma de cargos.
 * @param message mensaje de resultado.
 * @param charges cargos creados por la corrida.
 */
record BillingRunResponse(
        String id,
        String runCode,
        BillingRunType runType,
        BillingRunStatus status,
        LocalDateTime virtualTime,
        Instant createdT,
        LocalDateTime pinVirtualTimeT,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        int accountsProcessed,
        int chargesCreated,
        BigDecimal totalAmount,
        String message,
        List<BillingChargeResponse> charges
) {

    /**
     * Convierte una corrida y sus cargos en respuesta API.
     *
     * @param run corrida persistida.
     * @param charges cargos asociados a la corrida.
     * @return DTO de billing con detalle de cargos.
     */
    static BillingRunResponse from(BillingRun run, List<BillingCharge> charges) {
        return new BillingRunResponse(
                run.id(),
                run.runCode(),
                run.runType(),
                run.status(),
                run.virtualTime(),
                run.createdT(),
                run.pinVirtualTimeT(),
                run.startedAt(),
                run.finishedAt(),
                run.accountsProcessed(),
                run.chargesCreated(),
                run.totalAmount(),
                run.message(),
                charges.stream().map(BillingChargeResponse::from).toList()
        );
    }
}
