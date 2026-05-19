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
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Documento de credito que reduce el saldo pendiente de una invoice.
 *
 * <p>La nota de credito conserva su propio numero funcional y no elimina cargos ni lineas de la
 * factura original. El impacto financiero se refleja en el monto acreditado y en el saldo pendiente
 * de la invoice asociada.</p>
 */
@Entity
@Table(
        name = "credit_notes_t",
        uniqueConstraints = @UniqueConstraint(name = "uk_credit_notes_t_number", columnNames = "credit_note_number")
)
class CreditNote {

    private static final DateTimeFormatter CREDIT_NOTE_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Id
    @Column(length = 19, nullable = false)
    private String id;

    @Column(name = "credit_note_number", nullable = false, unique = true, length = 80)
    private String creditNoteNumber;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CreditNoteStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal taxAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDateTime issueDate;

    @Column(nullable = false)
    private LocalDateTime appliedAt;

    private LocalDateTime cancelledAt;

    @Column(length = 500)
    private String reason;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_t", updatable = false)
    private Instant createdT;

    @Column(name = "pin_virtual_time_t", updatable = false)
    private LocalDateTime pinVirtualTimeT;

    private Instant updatedAt;

    /**
     * Constructor requerido por JPA.
     */
    protected CreditNote() {
    }

    /**
     * Crea una nota de credito aplicada a una factura.
     *
     * @param invoice factura afectada.
     * @param creditNoteNumber numero funcional visible.
     * @param amount monto acreditado.
     * @param reason motivo de negocio.
     * @param notes detalle opcional.
     * @param issueDate fecha virtual de emision y aplicacion.
     */
    CreditNote(
            Invoice invoice,
            String creditNoteNumber,
            BigDecimal amount,
            String reason,
            String notes,
            LocalDateTime issueDate
    ) {
        var now = Instant.now();
        this.id = newCreditNoteId();
        this.creditNoteNumber = creditNoteNumber;
        this.invoice = invoice;
        this.account = invoice.account();
        this.status = CreditNoteStatus.APPLIED;
        this.currency = invoice.currency();
        this.subtotal = amount;
        this.taxAmount = BigDecimal.ZERO;
        this.totalAmount = amount;
        this.issueDate = issueDate;
        this.appliedAt = issueDate;
        this.reason = reason;
        this.notes = notes;
        this.createdAt = now;
        this.createdT = now;
        this.pinVirtualTimeT = issueDate;
        this.updatedAt = now;
    }

    String id() {
        return id;
    }

    String creditNoteNumber() {
        return creditNoteNumber;
    }

    Invoice invoice() {
        return invoice;
    }

    Account account() {
        return account;
    }

    CreditNoteStatus status() {
        return status;
    }

    Currency currency() {
        return currency;
    }

    BigDecimal subtotal() {
        return subtotal;
    }

    BigDecimal taxAmount() {
        return taxAmount;
    }

    BigDecimal totalAmount() {
        return totalAmount;
    }

    LocalDateTime issueDate() {
        return issueDate;
    }

    LocalDateTime appliedAt() {
        return appliedAt;
    }

    LocalDateTime cancelledAt() {
        return cancelledAt;
    }

    String reason() {
        return reason;
    }

    String notes() {
        return notes;
    }

    Instant createdAt() {
        return createdAt;
    }

    Instant createdT() {
        return createdT == null ? createdAt : createdT;
    }

    LocalDateTime pinVirtualTimeT() {
        return pinVirtualTimeT == null ? issueDate : pinVirtualTimeT;
    }

    Instant updatedAt() {
        return updatedAt;
    }

    void cancel(LocalDateTime cancelledAt, String reason) {
        if (status == CreditNoteStatus.CANCELLED) {
            throw new BusinessRuleException("La nota de credito ya esta cancelada.");
        }
        status = CreditNoteStatus.CANCELLED;
        this.cancelledAt = cancelledAt;
        this.notes = reason;
        updatedAt = Instant.now();
    }

    private static String newCreditNoteId() {
        return LocalDateTime.now().format(CREDIT_NOTE_ID_FORMAT)
                + ThreadLocalRandom.current().nextInt(10, 100);
    }
}
