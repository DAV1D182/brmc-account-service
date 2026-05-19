package com.brmc.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para notas de credito.
 */
record CreditNoteResponse(
        String id,
        String creditNoteNumber,
        String invoiceId,
        String invoiceNumber,
        String accountId,
        String accountOwnerName,
        CreditNoteStatus status,
        Currency currency,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        LocalDateTime issueDate,
        LocalDateTime appliedAt,
        LocalDateTime cancelledAt,
        String reason,
        String notes,
        Instant createdAt,
        Instant createdT,
        LocalDateTime pinVirtualTimeT,
        Instant updatedAt,
        List<CreditNoteLineResponse> lines
) {

    static CreditNoteResponse from(CreditNote creditNote) {
        return from(creditNote, List.of());
    }

    static CreditNoteResponse from(CreditNote creditNote, List<CreditNoteLine> lines) {
        return new CreditNoteResponse(
                creditNote.id(),
                creditNote.creditNoteNumber(),
                creditNote.invoice().id(),
                creditNote.invoice().invoiceNumber(),
                creditNote.account().id(),
                creditNote.account().ownerName(),
                creditNote.status(),
                creditNote.currency(),
                creditNote.subtotal(),
                creditNote.taxAmount(),
                creditNote.totalAmount(),
                creditNote.issueDate(),
                creditNote.appliedAt(),
                creditNote.cancelledAt(),
                creditNote.reason(),
                creditNote.notes(),
                creditNote.createdAt(),
                creditNote.createdT(),
                creditNote.pinVirtualTimeT(),
                creditNote.updatedAt(),
                lines.stream().map(CreditNoteLineResponse::from).toList()
        );
    }
}
