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
 * Entidad JPA que representa una factura generada desde billing.
 *
 * <p>La factura resume los cargos creados para una cuenta dentro de un {@link BillingRun}. Mantiene
 * montos de subtotal, impuestos, total, pago aplicado y saldo pendiente. La factura no reemplaza
 * {@link Bill}: funciona como documento consultable e imprimible para el cliente.</p>
 */
@Entity
@Table(
        name = "invoices_t",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_invoices_t_invoice_number", columnNames = "invoice_number"),
                @UniqueConstraint(name = "uk_invoices_t_run_account", columnNames = {"billing_run_id", "account_id"})
        }
)
class Invoice {

    private static final DateTimeFormatter INVOICE_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    @Id
    @Column(length = 19, nullable = false)
    private String id;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 80)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "billing_run_id")
    private BillingRun billingRun;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceType invoiceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal taxAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountPaid;

    @Column(precision = 19, scale = 2)
    private BigDecimal creditAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountDue;

    @Column(nullable = false)
    private LocalDateTime issueDate;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    private LocalDateTime periodStart;

    private LocalDateTime periodEnd;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @Column(length = 120)
    private String generatedBy;

    private LocalDateTime sentAt;

    private LocalDateTime paidAt;

    private LocalDateTime cancelledAt;

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
    protected Invoice() {
    }

    /**
     * Crea una factura emitida para una cuenta y corrida de billing.
     *
     * @param account cuenta facturada.
     * @param billingRun corrida que origina los cargos.
     * @param issueDate fecha virtual de emision.
     * @param periodStart inicio del periodo facturado.
     * @param periodEnd fin del periodo facturado.
     * @param generatedBy usuario o proceso que genero la factura.
     */
    Invoice(
            Account account,
            BillingRun billingRun,
            LocalDateTime issueDate,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            String invoiceNumber,
            String generatedBy
    ) {
        var now = Instant.now();
        this.id = newInvoiceId();
        this.invoiceNumber = invoiceNumber;
        this.account = account;
        this.billingRun = billingRun;
        this.status = InvoiceStatus.ISSUED;
        this.invoiceType = InvoiceType.DETAILED;
        this.currency = Currency.COP;
        this.subtotal = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.amountPaid = BigDecimal.ZERO;
        this.creditAmount = BigDecimal.ZERO;
        this.amountDue = BigDecimal.ZERO;
        this.issueDate = issueDate;
        this.dueDate = issueDate.plusDays(30);
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.generatedAt = issueDate;
        this.generatedBy = generatedBy;
        this.createdAt = now;
        this.createdT = now;
        this.pinVirtualTimeT = issueDate;
        this.updatedAt = now;
    }

    /**
     * Obtiene el identificador interno.
     *
     * @return id de factura.
     */
    String id() {
        return id;
    }

    /**
     * Obtiene el numero funcional de factura.
     *
     * @return numero visible para el usuario.
     */
    String invoiceNumber() {
        return invoiceNumber;
    }

    /**
     * Obtiene la cuenta facturada.
     *
     * @return cuenta asociada.
     */
    Account account() {
        return account;
    }

    /**
     * Obtiene la corrida de billing origen.
     *
     * @return corrida asociada o {@code null} para facturas manuales futuras.
     */
    BillingRun billingRun() {
        return billingRun;
    }

    /**
     * Obtiene el estado actual.
     *
     * @return estado de factura.
     */
    InvoiceStatus status() {
        return status;
    }

    /**
     * Obtiene el tipo de documento.
     *
     * @return SUMMARY o DETAILED.
     */
    InvoiceType invoiceType() {
        return invoiceType;
    }

    /**
     * Obtiene la moneda.
     *
     * @return moneda de la factura.
     */
    Currency currency() {
        return currency;
    }

    /**
     * Obtiene el subtotal antes de impuestos.
     *
     * @return subtotal de lineas.
     */
    BigDecimal subtotal() {
        return subtotal;
    }

    /**
     * Obtiene impuestos calculados.
     *
     * @return impuestos; en el alcance actual es cero.
     */
    BigDecimal taxAmount() {
        return taxAmount;
    }

    /**
     * Obtiene el total facturado.
     *
     * @return subtotal mas impuestos.
     */
    BigDecimal totalAmount() {
        return totalAmount;
    }

    /**
     * Obtiene el monto aplicado como pago.
     *
     * @return total pagado.
     */
    BigDecimal amountPaid() {
        return amountPaid;
    }

    /**
     * Obtiene el monto total aplicado mediante notas de credito.
     *
     * @return valor acreditado sobre la factura.
     */
    BigDecimal creditAmount() {
        return creditAmount == null ? BigDecimal.ZERO : creditAmount;
    }

    /**
     * Obtiene el saldo pendiente.
     *
     * @return total pendiente de pago.
     */
    BigDecimal amountDue() {
        return amountDue;
    }

    /**
     * Obtiene la fecha virtual de emision.
     *
     * @return fecha de emision.
     */
    LocalDateTime issueDate() {
        return issueDate;
    }

    /**
     * Obtiene la fecha virtual de vencimiento.
     *
     * @return fecha limite sugerida.
     */
    LocalDateTime dueDate() {
        return dueDate;
    }

    /**
     * Obtiene el inicio del periodo.
     *
     * @return fecha inicial o {@code null}.
     */
    LocalDateTime periodStart() {
        return periodStart;
    }

    /**
     * Obtiene el cierre del periodo.
     *
     * @return fecha final o {@code null}.
     */
    LocalDateTime periodEnd() {
        return periodEnd;
    }

    /**
     * Obtiene la fecha virtual de generacion.
     *
     * @return fecha de generacion.
     */
    LocalDateTime generatedAt() {
        return generatedAt;
    }

    /**
     * Obtiene el usuario o proceso generador.
     *
     * @return generador registrado.
     */
    String generatedBy() {
        return generatedBy;
    }

    /**
     * Obtiene la fecha de envio.
     *
     * @return fecha de envio o {@code null}.
     */
    LocalDateTime sentAt() {
        return sentAt;
    }

    /**
     * Obtiene la fecha en la que quedo pagada.
     *
     * @return fecha de pago completo o {@code null}.
     */
    LocalDateTime paidAt() {
        return paidAt;
    }

    /**
     * Obtiene la fecha de cancelacion.
     *
     * @return fecha de cancelacion o {@code null}.
     */
    LocalDateTime cancelledAt() {
        return cancelledAt;
    }

    /**
     * Obtiene notas operativas.
     *
     * @return notas o {@code null}.
     */
    String notes() {
        return notes;
    }

    /**
     * Obtiene la fecha real de persistencia.
     *
     * @return instante real de creacion.
     */
    Instant createdAt() {
        return createdAt;
    }

    /**
     * Obtiene el reloj real tecnico de creacion.
     *
     * @return instante real tecnico.
     */
    Instant createdT() {
        return createdT == null ? createdAt : createdT;
    }

    /**
     * Obtiene el pin virtual time usado.
     *
     * @return fecha virtual de negocio.
     */
    LocalDateTime pinVirtualTimeT() {
        return pinVirtualTimeT == null ? issueDate : pinVirtualTimeT;
    }

    /**
     * Obtiene la ultima fecha real de modificacion.
     *
     * @return instante de actualizacion.
     */
    Instant updatedAt() {
        return updatedAt;
    }

    /**
     * Recalcula los totales desde sus lineas.
     *
     * @param subtotal subtotal de lineas.
     * @param taxAmount impuestos de lineas.
     */
    void updateTotals(BigDecimal subtotal, BigDecimal taxAmount) {
        if (subtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("No se puede crear una invoice con total negativo.");
        }
        this.subtotal = subtotal;
        this.taxAmount = taxAmount;
        this.totalAmount = subtotal.add(taxAmount);
        this.amountDue = totalAmount.subtract(amountPaid).subtract(creditAmount());
        this.updatedAt = Instant.now();
    }

    /**
     * Marca la factura como enviada.
     *
     * @param sentAt fecha virtual de envio.
     */
    void markAsSent(LocalDateTime sentAt) {
        if (status == InvoiceStatus.CANCELLED) {
            throw new BusinessRuleException("No se puede enviar una invoice cancelada.");
        }
        if (status != InvoiceStatus.PAID
                && status != InvoiceStatus.PARTIALLY_PAID
                && status != InvoiceStatus.CREDITED
                && status != InvoiceStatus.PARTIALLY_CREDITED) {
            status = InvoiceStatus.SENT;
        }
        this.sentAt = sentAt;
        this.updatedAt = Instant.now();
    }

    /**
     * Cancela la factura.
     *
     * @param cancelledAt fecha virtual de cancelacion.
     * @param reason motivo registrado.
     */
    void cancel(LocalDateTime cancelledAt, String reason) {
        if (status == InvoiceStatus.PAID || status == InvoiceStatus.CREDITED) {
            throw new BusinessRuleException("No se puede cancelar una invoice pagada.");
        }
        this.status = InvoiceStatus.CANCELLED;
        this.cancelledAt = cancelledAt;
        this.notes = reason;
        this.updatedAt = Instant.now();
    }

    /**
     * Aplica un pago a la factura y actualiza su estado.
     *
     * @param amount monto positivo a aplicar.
     * @param paymentTime fecha virtual del pago.
     */
    void applyPayment(BigDecimal amount, LocalDateTime paymentTime) {
        if (status == InvoiceStatus.CANCELLED) {
            throw new BusinessRuleException("No se puede pagar una invoice cancelada.");
        }
        if (status == InvoiceStatus.CREDITED) {
            throw new BusinessRuleException("No se puede pagar una invoice cubierta por nota de credito.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("El pago de invoice debe ser mayor a cero.");
        }
        if (amount.compareTo(amountDue) > 0) {
            throw new BusinessRuleException("El pago no puede ser mayor al saldo pendiente de la invoice.");
        }

        amountPaid = amountPaid.add(amount);
        amountDue = totalAmount.subtract(amountPaid).subtract(creditAmount());
        if (amountDue.compareTo(BigDecimal.ZERO) == 0) {
            status = InvoiceStatus.PAID;
            paidAt = paymentTime;
        } else {
            status = InvoiceStatus.PARTIALLY_PAID;
        }
        updatedAt = Instant.now();
    }

    /**
     * Aplica una nota de credito a la factura y reduce el saldo pendiente.
     *
     * @param amount monto positivo de la nota.
     * @param creditTime fecha virtual de aplicacion.
     * @throws BusinessRuleException si la factura esta cerrada para creditos o el monto es invalido.
     */
    void applyCredit(BigDecimal amount, LocalDateTime creditTime) {
        if (status == InvoiceStatus.CANCELLED) {
            throw new BusinessRuleException("No se puede aplicar nota de credito a una invoice cancelada.");
        }
        if (status == InvoiceStatus.PAID || status == InvoiceStatus.CREDITED) {
            throw new BusinessRuleException("La invoice no tiene saldo pendiente para nota de credito.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("El monto de la nota de credito debe ser mayor a cero.");
        }
        if (amount.compareTo(amountDue) > 0) {
            throw new BusinessRuleException("La nota de credito no puede superar el saldo pendiente de la invoice.");
        }

        creditAmount = creditAmount().add(amount);
        amountDue = totalAmount.subtract(amountPaid).subtract(creditAmount());
        if (amountDue.compareTo(BigDecimal.ZERO) == 0) {
            status = amountPaid.compareTo(BigDecimal.ZERO) > 0 ? InvoiceStatus.PAID : InvoiceStatus.CREDITED;
        } else {
            status = amountPaid.compareTo(BigDecimal.ZERO) > 0
                    ? InvoiceStatus.PARTIALLY_PAID
                    : InvoiceStatus.PARTIALLY_CREDITED;
        }
        updatedAt = Instant.now();
    }

    /**
     * Reversa una nota de credito cancelada y restaura el saldo pendiente.
     *
     * @param amount monto de la nota cancelada.
     */
    void reverseCredit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("El monto a reversar debe ser mayor a cero.");
        }
        if (amount.compareTo(creditAmount()) > 0) {
            throw new BusinessRuleException("El monto a reversar excede el credito aplicado a la invoice.");
        }

        creditAmount = creditAmount().subtract(amount);
        amountDue = totalAmount.subtract(amountPaid).subtract(creditAmount());
        if (amountDue.compareTo(BigDecimal.ZERO) == 0) {
            status = amountPaid.compareTo(BigDecimal.ZERO) > 0 ? InvoiceStatus.PAID : InvoiceStatus.CREDITED;
        } else if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
            status = InvoiceStatus.PARTIALLY_PAID;
        } else if (creditAmount().compareTo(BigDecimal.ZERO) > 0) {
            status = InvoiceStatus.PARTIALLY_CREDITED;
        } else if (sentAt != null) {
            status = InvoiceStatus.SENT;
        } else {
            status = InvoiceStatus.ISSUED;
        }
        updatedAt = Instant.now();
    }

    private static String newInvoiceId() {
        return LocalDateTime.now().format(INVOICE_ID_FORMAT)
                + ThreadLocalRandom.current().nextInt(10, 100);
    }

}
