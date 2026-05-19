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
 * Entidad JPA que registra write-offs aplicados directamente sobre una cuenta.
 *
 * <p>A diferencia de {@link RefundRecord}, no referencia un pago origen. Su objetivo es dejar
 * trazabilidad de un ajuste financiero independiente creado desde el modulo de write-off.</p>
 */
@Entity
@Table(name = "write_offs_t")
class WriteOffRecord {

    @Id
    @Column(length = 19, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(length = 240)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_t", updatable = false)
    private Instant createdT;

    @Column(name = "pin_virtual_time_t", updatable = false)
    private LocalDateTime pinVirtualTimeT;

    /**
     * Constructor requerido por JPA para materializar write-offs persistidos.
     */
    protected WriteOffRecord() {
    }

    /**
     * Crea el registro especializado de write-off a partir de la transaccion aplicada.
     *
     * @param account cuenta ajustada.
     * @param transaction transaccion WRITE_OFF usada como fuente.
     */
    WriteOffRecord(Account account, AccountTransaction transaction) {
        this.id = transaction.id();
        this.account = account;
        this.amount = transaction.amount();
        this.currency = transaction.currency();
        this.description = transaction.description();
        this.createdAt = transaction.createdAt();
        this.createdT = transaction.createdT();
        this.pinVirtualTimeT = transaction.pinVirtualTimeT();
    }
}
