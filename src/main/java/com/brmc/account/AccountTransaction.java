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
 * Entidad JPA que representa un movimiento financiero dentro del historial de una cuenta.
 *
 * <p>Centraliza la trazabilidad de pagos, reembolsos, write-offs y cargos de billing en la tabla
 * {@code account_transactions_t}. Todas las transacciones se registran en COP como moneda de
 * impacto contable y conservan, cuando aplica, el monto, moneda original, tasa de conversion y
 * metodo de pago usado para construir el movimiento.</p>
 */
@Entity
@Table(name = "account_transactions_t")
class AccountTransaction {

    private static final DateTimeFormatter TRANSACTION_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Id
    @Column(length = 19, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(length = 3)
    private Currency currency;

    @Column(precision = 19, scale = 2)
    private BigDecimal originalAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 3)
    private Currency originalCurrency;

    @Column(precision = 19, scale = 6)
    private BigDecimal exchangeRate;

    @Column(length = 240)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private PaymentMethod paymentMethod;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_t", updatable = false)
    private Instant createdT;

    @Column(name = "pin_virtual_time_t", updatable = false)
    private LocalDateTime pinVirtualTimeT;

    /**
     * Constructor requerido por JPA para materializar transacciones persistidas.
     */
    protected AccountTransaction() {
    }

    /**
     * Crea una transaccion financiera con defaults de moneda y tasa.
     *
     * @param account cuenta propietaria de la transaccion.
     * @param type tipo funcional del movimiento.
     * @param amount monto en COP que impacta el saldo.
     * @param originalAmount monto original antes de conversion.
     * @param originalCurrency moneda original informada por la operacion.
     * @param exchangeRate tasa usada para convertir a COP.
     * @param description descripcion operativa visible en historial y reportes.
     * @param paymentMethod metodo de pago CL; solo aplica a pagos.
     */
    private AccountTransaction(
            Account account,
            TransactionType type,
            BigDecimal amount,
            BigDecimal originalAmount,
            Currency originalCurrency,
            BigDecimal exchangeRate,
            String description,
            PaymentMethod paymentMethod,
            LocalDateTime pinVirtualTimeT
    ) {
        var now = Instant.now();
        this.id = newTransactionId();
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.currency = Currency.COP;
        this.originalAmount = originalAmount;
        this.originalCurrency = originalCurrency == null ? Currency.COP : originalCurrency;
        this.exchangeRate = exchangeRate == null ? BigDecimal.ONE : exchangeRate;
        this.description = description;
        this.paymentMethod = paymentMethod;
        this.createdAt = now;
        this.createdT = now;
        this.pinVirtualTimeT = pinVirtualTimeT;
    }

    /**
     * Construye una transaccion de pago.
     *
     * @param account cuenta que recibe el pago.
     * @param amount monto convertido a COP.
     * @param originalAmount monto recibido en la moneda original.
     * @param originalCurrency moneda original del pago.
     * @param exchangeRate tasa aplicada para convertir a COP.
     * @param description descripcion del pago.
     * @param paymentMethod metodo CL usado para recibir el pago.
     * @return transaccion de tipo PAYMENT.
     */
    static AccountTransaction payment(
            Account account,
            BigDecimal amount,
            BigDecimal originalAmount,
            Currency originalCurrency,
            BigDecimal exchangeRate,
            String description,
            PaymentMethod paymentMethod,
            LocalDateTime pinVirtualTimeT
    ) {
        return new AccountTransaction(
                account,
                TransactionType.PAYMENT,
                amount,
                originalAmount,
                originalCurrency,
                exchangeRate,
                description,
                paymentMethod,
                pinVirtualTimeT
        );
    }

    /**
     * Construye una transaccion de reembolso asociada a un pago original.
     *
     * @param account cuenta que emite el reembolso.
     * @param amount monto en COP descontado del saldo.
     * @param originalAmount monto original del pago reembolsado.
     * @param originalCurrency moneda original del pago reembolsado.
     * @param exchangeRate tasa historica usada por el pago original.
     * @param description descripcion del reembolso.
     * @return transaccion de tipo REFUND.
     */
    static AccountTransaction refund(
            Account account,
            BigDecimal amount,
            BigDecimal originalAmount,
            Currency originalCurrency,
            BigDecimal exchangeRate,
            String description,
            LocalDateTime pinVirtualTimeT
    ) {
        return new AccountTransaction(
                account,
                TransactionType.REFUND,
                amount,
                originalAmount,
                originalCurrency,
                exchangeRate,
                description,
                null,
                pinVirtualTimeT
        );
    }

    /**
     * Construye una transaccion de write-off.
     *
     * @param account cuenta sobre la que se aplica el ajuste.
     * @param amount monto COP descontado.
     * @param description descripcion del ajuste.
     * @return transaccion de tipo WRITE_OFF.
     */
    static AccountTransaction writeOff(Account account, BigDecimal amount, String description, LocalDateTime pinVirtualTimeT) {
        return new AccountTransaction(
                account,
                TransactionType.WRITE_OFF,
                amount,
                amount,
                Currency.COP,
                BigDecimal.ONE,
                description,
                null,
                pinVirtualTimeT
        );
    }

    /**
     * Construye una transaccion derivada de billing.
     *
     * @param account cuenta facturada.
     * @param amount monto COP del cargo.
     * @param description descripcion del cargo generado.
     * @return transaccion de tipo BILLING_CHARGE.
     */
    static AccountTransaction billingCharge(Account account, BigDecimal amount, String description, LocalDateTime pinVirtualTimeT) {
        return new AccountTransaction(
                account,
                TransactionType.BILLING_CHARGE,
                amount,
                amount,
                Currency.COP,
                BigDecimal.ONE,
                description,
                null,
                pinVirtualTimeT
        );
    }

    /**
     * Obtiene el identificador funcional de la transaccion.
     *
     * @return id temporal con sufijo aleatorio para reducir colisiones.
     */
    String id() {
        return id;
    }

    /**
     * Obtiene el tipo de movimiento financiero.
     *
     * @return tipo de transaccion.
     */
    TransactionType type() {
        return type;
    }

    /**
     * Obtiene el monto que impacta el saldo de la cuenta.
     *
     * @return monto en COP.
     */
    BigDecimal amount() {
        return amount;
    }

    /**
     * Obtiene la moneda contable del movimiento.
     *
     * @return moneda persistida o COP cuando no existe valor historico.
     */
    Currency currency() {
        return currency == null ? Currency.COP : currency;
    }

    /**
     * Obtiene el monto original de la operacion.
     *
     * @return monto original o el monto COP cuando no se informo valor historico.
     */
    BigDecimal originalAmount() {
        return originalAmount == null ? amount : originalAmount;
    }

    /**
     * Obtiene la moneda original de la operacion.
     *
     * @return moneda original o COP por defecto.
     */
    Currency originalCurrency() {
        return originalCurrency == null ? Currency.COP : originalCurrency;
    }

    /**
     * Obtiene la tasa de conversion usada por la operacion.
     *
     * @return tasa guardada o 1 cuando no hubo conversion.
     */
    BigDecimal exchangeRate() {
        return exchangeRate == null ? BigDecimal.ONE : exchangeRate;
    }

    /**
     * Obtiene la descripcion operativa del movimiento.
     *
     * @return texto descriptivo del historial.
     */
    String description() {
        return description;
    }

    /**
     * Obtiene el metodo de pago CL cuando el movimiento es un pago.
     *
     * @return metodo de pago o {@code null} para movimientos no asociados a pago.
     */
    PaymentMethod paymentMethod() {
        return paymentMethod;
    }

    /**
     * Obtiene el instante en que se creo la transaccion.
     *
     * @return fecha/hora de creacion.
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

    private static String newTransactionId() {
        return LocalDateTime.now().format(TRANSACTION_ID_FORMAT)
                + ThreadLocalRandom.current().nextInt(10, 100);
    }
}
