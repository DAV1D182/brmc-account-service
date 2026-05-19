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
 * Linea de detalle de una factura.
 *
 * <p>Cada linea conserva un snapshot del cargo de billing: descripcion, tipo, cantidad, monto y
 * moneda. Esto permite que la factura siga siendo legible aunque el producto o servicio cambien
 * despues de la emision.</p>
 */
@Entity
@Table(name = "invoice_lines_t")
class InvoiceLine {

    private static final DateTimeFormatter INVOICE_LINE_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Id
    @Column(length = 19, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "billing_charge_id")
    private BillingCharge billingCharge;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id")
    private BrmService service;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceLineType lineType;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal taxAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    private LocalDateTime chargeDate;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_t", updatable = false)
    private Instant createdT;

    @Column(name = "pin_virtual_time_t", updatable = false)
    private LocalDateTime pinVirtualTimeT;

    /**
     * Constructor requerido por JPA.
     */
    protected InvoiceLine() {
    }

    /**
     * Crea una linea a partir de un cargo de billing.
     *
     * @param invoice factura propietaria.
     * @param charge cargo fuente.
     */
    InvoiceLine(Invoice invoice, BillingCharge charge) {
        var now = Instant.now();
        this.id = newInvoiceLineId();
        this.invoice = invoice;
        this.billingCharge = charge;
        this.account = charge.account();
        this.service = charge.service();
        this.product = charge.product();
        this.lineType = charge.chargeType() == ChargeType.ONE_TIME
                ? InvoiceLineType.ONE_TIME
                : InvoiceLineType.RECURRING;
        this.description = charge.description();
        this.quantity = BigDecimal.ONE;
        this.unitAmount = charge.amount();
        this.subtotal = quantity.multiply(unitAmount);
        this.taxAmount = BigDecimal.ZERO;
        this.totalAmount = subtotal.add(taxAmount);
        this.currency = charge.currency();
        this.chargeDate = charge.chargeDate();
        this.createdAt = now;
        this.createdT = now;
        this.pinVirtualTimeT = charge.pinVirtualTimeT();
    }

    String id() {
        return id;
    }

    Invoice invoice() {
        return invoice;
    }

    BillingCharge billingCharge() {
        return billingCharge;
    }

    Account account() {
        return account;
    }

    BrmService service() {
        return service;
    }

    Product product() {
        return product;
    }

    InvoiceLineType lineType() {
        return lineType;
    }

    String description() {
        return description;
    }

    BigDecimal quantity() {
        return quantity;
    }

    BigDecimal unitAmount() {
        return unitAmount;
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

    Currency currency() {
        return currency;
    }

    LocalDateTime chargeDate() {
        return chargeDate;
    }

    Instant createdAt() {
        return createdAt;
    }

    Instant createdT() {
        return createdT == null ? createdAt : createdT;
    }

    LocalDateTime pinVirtualTimeT() {
        return pinVirtualTimeT == null ? chargeDate : pinVirtualTimeT;
    }

    private static String newInvoiceLineId() {
        return LocalDateTime.now().format(INVOICE_LINE_ID_FORMAT)
                + ThreadLocalRandom.current().nextInt(10, 100);
    }
}
