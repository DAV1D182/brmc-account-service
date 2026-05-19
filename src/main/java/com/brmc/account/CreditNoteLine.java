package com.brmc.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Linea de detalle de una nota de credito.
 *
 * <p>Puede referenciar una linea original de invoice o representar un credito general sobre la
 * factura. Conserva descripcion, monto y moneda como snapshot documental.</p>
 */
@Entity
@Table(name = "credit_note_lines_t")
class CreditNoteLine {

    private static final DateTimeFormatter CREDIT_NOTE_LINE_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Id
    @Column(length = 19, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "credit_note_id", nullable = false)
    private CreditNote creditNote;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "invoice_line_id")
    private InvoiceLine invoiceLine;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceLineType lineType;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_t", updatable = false)
    private Instant createdT;

    @Column(name = "pin_virtual_time_t", updatable = false)
    private LocalDateTime pinVirtualTimeT;

    /**
     * Constructor requerido por JPA.
     */
    protected CreditNoteLine() {
    }

    CreditNoteLine(CreditNote creditNote, InvoiceLine invoiceLine, String description, BigDecimal amount, LocalDateTime virtualTime) {
        var now = Instant.now();
        this.id = newCreditNoteLineId();
        this.creditNote = creditNote;
        this.invoiceLine = invoiceLine;
        this.invoice = creditNote.invoice();
        this.account = creditNote.account();
        this.lineType = invoiceLine == null ? InvoiceLineType.ADJUSTMENT : invoiceLine.lineType();
        this.description = description;
        this.amount = amount;
        this.currency = creditNote.currency();
        this.createdAt = now;
        this.createdT = now;
        this.pinVirtualTimeT = virtualTime;
    }

    String id() {
        return id;
    }

    CreditNote creditNote() {
        return creditNote;
    }

    InvoiceLine invoiceLine() {
        return invoiceLine;
    }

    Invoice invoice() {
        return invoice;
    }

    Account account() {
        return account;
    }

    InvoiceLineType lineType() {
        return lineType;
    }

    String description() {
        return description;
    }

    BigDecimal amount() {
        return amount;
    }

    Currency currency() {
        return currency;
    }

    Instant createdAt() {
        return createdAt;
    }

    Instant createdT() {
        return createdT == null ? createdAt : createdT;
    }

    LocalDateTime pinVirtualTimeT() {
        return pinVirtualTimeT;
    }

    private static String newCreditNoteLineId() {
        return LocalDateTime.now().format(CREDIT_NOTE_LINE_ID_FORMAT)
                + ThreadLocalRandom.current().nextInt(10, 100);
    }
}
