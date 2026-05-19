package com.brmc.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para disputas.
 *
 * @param id identificador de la disputa.
 * @param accountId cuenta asociada.
 * @param ownerName titular de la cuenta.
 * @param amount monto disputado.
 * @param currency moneda del monto.
 * @param reason motivo registrado.
 * @param status estado del caso.
 * @param resolutionNote nota de resolucion.
 * @param createdAt fecha de creacion.
 * @param resolvedAt fecha de cierre, si existe.
 */
record DisputeResponse(
        String id,
        String accountId,
        String ownerName,
        BigDecimal amount,
        Currency currency,
        String reason,
        DisputeStatus status,
        String resolutionNote,
        Instant createdAt,
        Instant createdT,
        LocalDateTime pinVirtualTimeT,
        Instant resolvedAt,
        LocalDateTime resolvedPinVirtualTimeT
) {

    /**
     * Convierte una disputa persistida en respuesta API.
     *
     * @param dispute disputa de dominio.
     * @return DTO con datos de cuenta y estado de resolucion.
     */
    static DisputeResponse from(Dispute dispute) {
        return new DisputeResponse(
                dispute.id(),
                dispute.account().id(),
                dispute.account().ownerName(),
                dispute.amount(),
                Currency.COP,
                dispute.reason(),
                dispute.status(),
                dispute.resolutionNote(),
                dispute.createdAt(),
                dispute.createdT(),
                dispute.pinVirtualTimeT(),
                dispute.resolvedAt(),
                dispute.resolvedPinVirtualTimeT()
        );
    }
}
