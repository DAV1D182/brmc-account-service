package com.brmc.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para facturas.
 */
record InvoiceResponse(
        String id,
        String invoiceNumber,
        String accountId,
        String accountOwnerName,
        String accountEmail,
        String billingRunId,
        String billingRunCode,
        InvoiceStatus status,
        InvoiceType invoiceType,
        Currency currency,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        BigDecimal creditAmount,
        BigDecimal amountDue,
        LocalDateTime issueDate,
        LocalDateTime dueDate,
        LocalDateTime periodStart,
        LocalDateTime periodEnd,
        LocalDateTime generatedAt,
        String generatedBy,
        LocalDateTime sentAt,
        LocalDateTime paidAt,
        LocalDateTime cancelledAt,
        String notes,
        Instant createdAt,
        Instant createdT,
        LocalDateTime pinVirtualTimeT,
        Instant updatedAt,
        List<InvoiceLineResponse> lines
) {

    /**
     * Convierte una factura sin lineas embebidas.
     *
     * @param invoice factura persistida.
     * @return DTO de cabecera.
     */
    static InvoiceResponse from(Invoice invoice) {
        return from(invoice, List.of());
    }

    /**
     * Convierte una factura con lineas.
     *
     * @param invoice factura persistida.
     * @param lines lineas asociadas.
     * @return DTO completo.
     */
    static InvoiceResponse from(Invoice invoice, List<InvoiceLine> lines) {
        return new InvoiceResponse(
                invoice.id(),
                invoice.invoiceNumber(),
                invoice.account().id(),
                invoice.account().ownerName(),
                invoice.account().email(),
                invoice.billingRun() == null ? null : invoice.billingRun().id(),
                invoice.billingRun() == null ? null : invoice.billingRun().runCode(),
                invoice.status(),
                invoice.invoiceType(),
                invoice.currency(),
                invoice.subtotal(),
                invoice.taxAmount(),
                invoice.totalAmount(),
                invoice.amountPaid(),
                invoice.creditAmount(),
                invoice.amountDue(),
                invoice.issueDate(),
                invoice.dueDate(),
                invoice.periodStart(),
                invoice.periodEnd(),
                invoice.generatedAt(),
                invoice.generatedBy(),
                invoice.sentAt(),
                invoice.paidAt(),
                invoice.cancelledAt(),
                invoice.notes(),
                invoice.createdAt(),
                invoice.createdT(),
                invoice.pinVirtualTimeT(),
                invoice.updatedAt(),
                lines.stream().map(InvoiceLineResponse::from).toList()
        );
    }
}
