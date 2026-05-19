package com.brmc.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para settlements de disputas.
 *
 * @param id identificador del settlement.
 * @param disputeId disputa asociada.
 * @param accountId cuenta relacionada.
 * @param ownerName titular de la cuenta.
 * @param amount monto acordado.
 * @param currency moneda del settlement.
 * @param note nota del acuerdo.
 * @param createdAt fecha de creacion.
 */
record DisputeSettlementResponse(
        String id,
        String disputeId,
        String accountId,
        String ownerName,
        BigDecimal amount,
        Currency currency,
        String note,
        Instant createdAt,
        Instant createdT,
        LocalDateTime pinVirtualTimeT
) {

    /**
     * Convierte un settlement persistido en respuesta API.
     *
     * @param settlement acuerdo de disputa.
     * @return DTO con datos de disputa, cuenta y monto.
     */
    static DisputeSettlementResponse from(DisputeSettlement settlement) {
        return new DisputeSettlementResponse(
                settlement.id(),
                settlement.dispute().id(),
                settlement.account().id(),
                settlement.account().ownerName(),
                settlement.amount(),
                settlement.currency(),
                settlement.note(),
                settlement.createdAt(),
                settlement.createdT(),
                settlement.pinVirtualTimeT()
        );
    }
}
