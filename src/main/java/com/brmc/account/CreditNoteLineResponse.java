package com.brmc.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * DTO de linea de nota de credito.
 */
record CreditNoteLineResponse(
        String id,
        String creditNoteId,
        String invoiceLineId,
        String invoiceId,
        String accountId,
        InvoiceLineType lineType,
        String description,
        BigDecimal amount,
        Currency currency,
        Instant createdAt,
        Instant createdT,
        LocalDateTime pinVirtualTimeT
) {

    static CreditNoteLineResponse from(CreditNoteLine line) {
        return new CreditNoteLineResponse(
                line.id(),
                line.creditNote().id(),
                line.invoiceLine() == null ? null : line.invoiceLine().id(),
                line.invoice().id(),
                line.account().id(),
                line.lineType(),
                line.description(),
                line.amount(),
                line.currency(),
                line.createdAt(),
                line.createdT(),
                line.pinVirtualTimeT()
        );
    }
}
