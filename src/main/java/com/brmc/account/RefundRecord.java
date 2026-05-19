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

/**
 * Entidad JPA que representa un reembolso total de un pago registrado.
 *
 * <p>La tabla {@code refunds_t} mantiene la relacion obligatoria con {@link PaymentRecord}; por
 * eso el reembolso no opera como un write-off libre, sino como devolucion del pago seleccionado.
 * En la regla actual del servicio solo se permite un reembolso por pago.</p>
 */
@Entity
@Table(name = "refunds_t")
class RefundRecord {

    @Id
    @Column(length = 19, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private PaymentRecord payment;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal originalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency originalCurrency;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal exchangeRate;

    @Column(length = 240)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_t", updatable = false)
    private Instant createdT;

    @Column(name = "pin_virtual_time_t", updatable = false)
    private LocalDateTime pinVirtualTimeT;

    /**
     * Constructor requerido por JPA para materializar reembolsos persistidos.
     */
    protected RefundRecord() {
    }

    /**
     * Crea el registro de reembolso usando el pago origen y la transaccion aplicada a la cuenta.
     *
     * @param account cuenta que emite el reembolso.
     * @param payment pago original reembolsado.
     * @param transaction transaccion REFUND que redujo el saldo.
     */
    RefundRecord(Account account, PaymentRecord payment, AccountTransaction transaction) {
        this.id = transaction.id();
        this.account = account;
        this.payment = payment;
        this.amount = transaction.amount();
        this.currency = transaction.currency();
        this.originalAmount = transaction.originalAmount();
        this.originalCurrency = transaction.originalCurrency();
        this.exchangeRate = transaction.exchangeRate();
        this.description = transaction.description();
        this.createdAt = transaction.createdAt();
        this.createdT = transaction.createdT();
        this.pinVirtualTimeT = transaction.pinVirtualTimeT();
    }

    /**
     * Obtiene el identificador del reembolso.
     *
     * @return id compartido con la transaccion financiera.
     */
    String id() {
        return id;
    }

    /**
     * Obtiene el pago origen del reembolso.
     *
     * @return pago reembolsado.
     */
    PaymentRecord payment() {
        return payment;
    }

    /**
     * Obtiene el monto contable reembolsado.
     *
     * @return monto descontado en COP.
     */
    BigDecimal amount() {
        return amount;
    }

    /**
     * Obtiene la moneda contable del reembolso.
     *
     * @return moneda de impacto en saldo.
     */
    Currency currency() {
        return currency;
    }

    /**
     * Obtiene el monto original del pago reembolsado.
     *
     * @return monto original asociado al pago.
     */
    BigDecimal originalAmount() {
        return originalAmount;
    }

    /**
     * Obtiene la moneda original del pago reembolsado.
     *
     * @return moneda original asociada al pago.
     */
    Currency originalCurrency() {
        return originalCurrency;
    }

    /**
     * Obtiene la tasa historica usada por el pago origen.
     *
     * @return tasa de conversion guardada.
     */
    BigDecimal exchangeRate() {
        return exchangeRate;
    }

    /**
     * Obtiene la fecha de registro del reembolso.
     *
     * @return instante de creacion heredado de la transaccion.
     */
    Instant createdAt() {
        return createdAt;
    }

    Instant createdT() {
        return createdT == null ? createdAt : createdT;
    }

    LocalDateTime pinVirtualTimeT() {
        return pinVirtualTimeT;
    }
}
