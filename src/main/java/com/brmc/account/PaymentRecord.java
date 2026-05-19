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
 * Entidad JPA que materializa los pagos recibidos en la tabla {@code payments_t}.
 *
 * <p>Complementa el historial financiero guardado en {@link AccountTransaction} con una vista
 * especializada de pagos. El identificador se toma de la transaccion origen para permitir
 * relacionar reembolsos contra el pago exacto que los origina.</p>
 */
@Entity
@Table(name = "payments_t")
class PaymentRecord {

    @Id
    @Column(length = 19, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

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

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private PaymentMethod paymentMethod;

    @Column(precision = 19, scale = 2)
    private BigDecimal allocatedAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal unallocatedAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PaymentAllocationStatus allocationStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_t", updatable = false)
    private Instant createdT;

    @Column(name = "pin_virtual_time_t", updatable = false)
    private LocalDateTime pinVirtualTimeT;

    /**
     * Constructor requerido por JPA para materializar pagos persistidos.
     */
    protected PaymentRecord() {
    }

    /**
     * Crea el registro especializado de pago a partir de la transaccion ya aplicada a la cuenta.
     *
     * @param account cuenta que recibio el pago.
     * @param transaction transaccion PAYMENT usada como fuente de datos.
     */
    PaymentRecord(Account account, AccountTransaction transaction) {
        this.id = transaction.id();
        this.account = account;
        this.amount = transaction.amount();
        this.currency = transaction.currency();
        this.originalAmount = transaction.originalAmount();
        this.originalCurrency = transaction.originalCurrency();
        this.exchangeRate = transaction.exchangeRate();
        this.description = transaction.description();
        this.paymentMethod = transaction.paymentMethod();
        this.allocatedAmount = BigDecimal.ZERO;
        this.unallocatedAmount = transaction.amount();
        this.allocationStatus = PaymentAllocationStatus.UNALLOCATED;
        this.createdAt = transaction.createdAt();
        this.createdT = transaction.createdT();
        this.pinVirtualTimeT = transaction.pinVirtualTimeT();
    }

    /**
     * Obtiene el identificador del pago.
     *
     * @return id compartido con la transaccion financiera.
     */
    String id() {
        return id;
    }

    /**
     * Obtiene la cuenta asociada al pago.
     *
     * @return cuenta propietaria.
     */
    Account account() {
        return account;
    }

    /**
     * Obtiene el monto contable del pago.
     *
     * @return monto convertido a COP.
     */
    BigDecimal amount() {
        return amount;
    }

    /**
     * Obtiene la moneda contable del pago.
     *
     * @return moneda de impacto en saldo.
     */
    Currency currency() {
        return currency;
    }

    /**
     * Obtiene el monto original recibido.
     *
     * @return monto antes de conversion.
     */
    BigDecimal originalAmount() {
        return originalAmount;
    }

    /**
     * Obtiene la moneda original del pago.
     *
     * @return COP o USD segun la solicitud.
     */
    Currency originalCurrency() {
        return originalCurrency;
    }

    /**
     * Obtiene la tasa aplicada al pago.
     *
     * @return tasa de conversion a COP.
     */
    BigDecimal exchangeRate() {
        return exchangeRate;
    }

    /**
     * Obtiene la fecha de registro del pago.
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

    /**
     * Obtiene el metodo CL usado para recibir el pago.
     *
     * @return metodo de pago configurado en la operacion.
     */
    PaymentMethod paymentMethod() {
        return paymentMethod;
    }

    /**
     * Obtiene el monto aplicado a bills o items.
     *
     * @return monto ya asignado; inicialmente cero.
     */
    BigDecimal allocatedAmount() {
        return allocatedAmount == null ? BigDecimal.ZERO : allocatedAmount;
    }

    /**
     * Obtiene el monto pendiente de asignacion.
     *
     * @return remanente disponible para aplicar a bills o items.
     */
    BigDecimal unallocatedAmount() {
        return unallocatedAmount == null ? amount : unallocatedAmount;
    }

    /**
     * Obtiene el estado de asignacion del pago.
     *
     * @return estado de asignacion actual.
     */
    PaymentAllocationStatus allocationStatus() {
        return allocationStatus == null ? PaymentAllocationStatus.UNALLOCATED : allocationStatus;
    }

    /**
     * Obtiene la invoice a la que se aplico el pago.
     *
     * @return factura asociada o {@code null} si el pago sigue no asignado.
     */
    Invoice invoice() {
        return invoice;
    }

    /**
     * Marca el pago como aplicado a una invoice.
     *
     * @param invoice factura destino.
     * @param amountToAllocate monto aplicado.
     */
    void applyAllocation(Invoice invoice, BigDecimal amountToAllocate) {
        if (amountToAllocate == null || amountToAllocate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("El monto a asignar debe ser mayor a cero.");
        }
        if (amountToAllocate.compareTo(unallocatedAmount()) > 0) {
            throw new BusinessRuleException("El monto a asignar excede el pago no asignado.");
        }

        this.invoice = invoice;
        this.allocatedAmount = allocatedAmount().add(amountToAllocate);
        this.unallocatedAmount = unallocatedAmount().subtract(amountToAllocate);
        this.allocationStatus = unallocatedAmount.compareTo(BigDecimal.ZERO) == 0
                ? PaymentAllocationStatus.ALLOCATED
                : PaymentAllocationStatus.PARTIALLY_ALLOCATED;
    }
}
