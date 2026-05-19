package com.brmc.account;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entidad JPA que representa una cuenta financiera BRMC.
 *
 * <p>La cuenta concentra el saldo base en COP, los datos del titular y el estado operativo. Sus
 * metodos de dominio aplican las reglas de saldo usadas por el sistema: los pagos incrementan el
 * saldo, los reembolsos y write-offs lo disminuyen con validacion de disponibilidad, y los cargos
 * de billing lo disminuyen incluso si queda negativo para representar deuda pendiente.</p>
 */
@Entity
@Table(name = "accounts_t")
class Account {

    @Id
    @Column(length = 14, nullable = false)
    private String id;

    @Column(nullable = false, length = 120)
    private String ownerName;

    @Column(length = 40)
    private String phoneNumber;

    @Column(length = 160)
    private String email;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "billing_dom")
    private Integer billingDom;

    @Column(name = "billing_cycle", length = 20)
    private String billingCycle;

    @Column(name = "bill_no", length = 40)
    private String billNo;

    @Column(name = "owner_username", length = 60)
    private String ownerUsername;

    @Enumerated(EnumType.STRING)
    @Column(length = 3)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_t", updatable = false)
    private Instant createdT;

    @Column(name = "pin_virtual_time_t", updatable = false)
    private LocalDateTime pinVirtualTimeT;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountTransaction> transactions = new ArrayList<>();

    /**
     * Constructor requerido por JPA para materializar la entidad desde la base de datos.
     */
    protected Account() {
    }

    /**
     * Crea una cuenta activa con moneda base COP.
     *
     * @param id identificador funcional generado con formato temporal.
     * @param ownerName nombre del titular de la cuenta.
     * @param phoneNumber numero de contacto asociado a la cuenta.
     * @param email correo de contacto asociado a la cuenta.
     * @param initialBalance saldo inicial disponible en COP.
     * @param billingDom dia del mes usado para ciclos de billing.
     * @param billingCycle ciclo de facturacion inicial.
     * @param pinVirtualTimeT fecha virtual vigente al crear la cuenta.
     */
    Account(
            String id,
            String ownerName,
            String phoneNumber,
            String email,
            BigDecimal initialBalance,
            Integer billingDom,
            String billingCycle,
            LocalDateTime pinVirtualTimeT
    ) {
        var now = Instant.now();
        this.id = id;
        this.ownerName = ownerName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.balance = initialBalance;
        this.billingDom = billingDom == null ? defaultBillingDom(pinVirtualTimeT) : billingDom;
        this.billingCycle = billingCycle == null || billingCycle.isBlank() ? "MONTHLY" : billingCycle;
        this.billNo = "BILL-" + id;
        this.currency = Currency.COP;
        this.status = AccountStatus.ACTIVE;
        this.createdAt = now;
        this.createdT = now;
        this.pinVirtualTimeT = pinVirtualTimeT;
    }

    /**
     * Obtiene el identificador funcional de la cuenta.
     *
     * @return id de cuenta usado por API, UI y relaciones de base de datos.
     */
    String id() {
        return id;
    }

    /**
     * Obtiene el nombre del titular registrado.
     *
     * @return nombre del titular.
     */
    String ownerName() {
        return ownerName;
    }

    /**
     * Obtiene el numero telefonico registrado.
     *
     * @return numero de telefono de la cuenta.
     */
    String phoneNumber() {
        return phoneNumber;
    }

    /**
     * Obtiene el correo electronico registrado.
     *
     * @return correo de contacto.
     */
    String email() {
        return email;
    }

    /**
     * Obtiene el saldo neto actual de la cuenta.
     *
     * @return saldo en COP; puede ser negativo cuando billing genera deuda.
     */
    BigDecimal balance() {
        return balance;
    }

    Integer billingDom() {
        if (billingDom != null) {
            return billingDom;
        }
        return pinVirtualTimeT == null ? null : pinVirtualTimeT.getDayOfMonth();
    }

    String billingCycle() {
        return billingCycle == null ? "MONTHLY" : billingCycle;
    }

    String billNo() {
        return billNo == null ? "BILL-" + id : billNo;
    }

    String ownerUsername() {
        return ownerUsername == null ? "" : ownerUsername;
    }

    /**
     * Obtiene la moneda base de la cuenta.
     *
     * @return moneda configurada o COP cuando el dato historico es nulo.
     */
    Currency currency() {
        return currency == null ? Currency.COP : currency;
    }

    /**
     * Obtiene el estado operativo de la cuenta.
     *
     * @return estado ACTIVE o CLOSED.
     */
    AccountStatus status() {
        return status;
    }

    /**
     * Obtiene la fecha de creacion de la cuenta.
     *
     * @return instante de creacion persistido.
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
     * Expone las transacciones de la cuenta como lista de solo lectura.
     *
     * @return vista inmodificable de pagos, reembolsos, write-offs y cargos de billing.
     */
    List<AccountTransaction> transactions() {
        return Collections.unmodifiableList(transactions);
    }

    /**
     * Registra un pago recibido y aumenta el saldo de la cuenta.
     *
     * @param amount monto convertido a COP que impacta el saldo.
     * @param originalAmount monto recibido antes de conversion.
     * @param originalCurrency moneda original del pago.
     * @param exchangeRate tasa usada para convertir a COP.
     * @param description descripcion operativa del pago.
     * @param paymentMethod metodo de pago CL usado.
     * @return transaccion financiera creada.
     * @throws AccountClosedException si la cuenta esta cerrada.
     */
    AccountTransaction receivePayment(
            BigDecimal amount,
            BigDecimal originalAmount,
            Currency originalCurrency,
            BigDecimal exchangeRate,
            String description,
            PaymentMethod paymentMethod,
            LocalDateTime pinVirtualTimeT
    ) {
        ensureActive();
        balance = balance.add(amount);
        var transaction = AccountTransaction.payment(
                this,
                amount,
                originalAmount,
                originalCurrency,
                exchangeRate,
                description,
                paymentMethod,
                pinVirtualTimeT
        );
        transactions.add(transaction);
        return transaction;
    }

    /**
     * Emite un reembolso y reduce el saldo disponible.
     *
     * @param amount monto COP a reembolsar.
     * @param description descripcion del reembolso.
     * @return transaccion de reembolso creada.
     * @throws AccountClosedException si la cuenta esta cerrada.
     * @throws InsufficientBalanceException si el saldo es menor al monto solicitado.
     */
    AccountTransaction sendRefund(BigDecimal amount, String description, LocalDateTime pinVirtualTimeT) {
        ensureActive();
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("La cuenta no tiene saldo suficiente para emitir el reembolso.");
        }

        balance = balance.subtract(amount);
        var transaction = AccountTransaction.refund(this, amount, amount, Currency.COP, BigDecimal.ONE, description, pinVirtualTimeT);
        transactions.add(transaction);
        return transaction;
    }

    /**
     * Aplica un write-off como ajuste independiente del pago original.
     *
     * @param amount monto COP a descontar.
     * @param description descripcion del ajuste.
     * @return transaccion de write-off creada.
     * @throws AccountClosedException si la cuenta esta cerrada.
     * @throws InsufficientBalanceException si el saldo es menor al ajuste.
     */
    AccountTransaction writeOff(BigDecimal amount, String description, LocalDateTime pinVirtualTimeT) {
        ensureActive();
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("La cuenta no tiene saldo suficiente para aplicar el write-off.");
        }

        balance = balance.subtract(amount);
        var transaction = AccountTransaction.writeOff(this, amount, description, pinVirtualTimeT);
        transactions.add(transaction);
        return transaction;
    }

    /**
     * Aplica un cargo de billing sobre la cuenta.
     *
     * <p>A diferencia de reembolso y write-off, este metodo no exige saldo disponible. Un saldo
     * negativo representa deuda generada por productos facturados.</p>
     *
     * @param amount monto COP del cargo.
     * @param description descripcion generada por billing.
     * @return transaccion de cargo creada.
     * @throws AccountClosedException si la cuenta esta cerrada.
     */
    AccountTransaction applyBillingCharge(BigDecimal amount, String description, LocalDateTime pinVirtualTimeT) {
        ensureActive();
        // En este proyecto el saldo representa credito/deuda neta: pagos suman y cargos de billing restan.
        balance = balance.subtract(amount);
        var transaction = AccountTransaction.billingCharge(this, amount, description, pinVirtualTimeT);
        transactions.add(transaction);
        return transaction;
    }

    /**
     * Cierra logicamente la cuenta sin eliminar sus datos.
     *
     * <p>Una cuenta cerrada queda disponible para consulta y auditoria, pero bloquea nuevas
     * operaciones financieras, de servicios y de billing.</p>
     */
    void close() {
        status = AccountStatus.CLOSED;
    }

    void assignOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    private void ensureActive() {
        if (status == AccountStatus.CLOSED) {
            throw new AccountClosedException("La cuenta esta cerrada y no permite pagos ni reembolsos.");
        }
    }

    private Integer defaultBillingDom(LocalDateTime pinVirtualTimeT) {
        return pinVirtualTimeT == null ? LocalDateTime.now().getDayOfMonth() : pinVirtualTimeT.getDayOfMonth();
    }
}
