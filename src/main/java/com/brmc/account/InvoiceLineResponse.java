package com.brmc.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para lineas de factura.
 */
record InvoiceLineResponse(
        String id,
        String invoiceId,
        String billingChargeId,
        String accountId,
        String serviceId,
        String serviceCode,
        String productId,
        String productCode,
        String productName,
        InvoiceLineType lineType,
        String description,
        BigDecimal quantity,
        BigDecimal unitAmount,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        Currency currency,
        LocalDateTime chargeDate,
        Instant createdAt,
        Instant createdT,
        LocalDateTime pinVirtualTimeT
) {

    /**
     * Convierte una linea persistida en respuesta API.
     *
     * @param line linea de factura.
     * @return DTO de linea.
     */
    static InvoiceLineResponse from(InvoiceLine line) {
        return new InvoiceLineResponse(
                line.id(),
                line.invoice().id(),
                line.billingCharge() == null ? null : line.billingCharge().id(),
                line.account().id(),
                line.service() == null ? null : line.service().id(),
                line.service() == null ? null : line.service().serviceCode(),
                line.product() == null ? null : line.product().id(),
                line.product() == null ? null : line.product().code(),
                line.product() == null ? null : line.product().name(),
                line.lineType(),
                line.description(),
                line.quantity(),
                line.unitAmount(),
                line.subtotal(),
                line.taxAmount(),
                line.totalAmount(),
                line.currency(),
                line.chargeDate(),
                line.createdAt(),
                line.createdT(),
                line.pinVirtualTimeT()
        );
    }
}
