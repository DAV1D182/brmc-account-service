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
 * Entidad JPA que registra el acuerdo de cierre de una disputa.
 *
 * <p>El settlement se crea desde {@link AccountService#createDisputeSettlement(String, BigDecimal,
 * String)} y queda asociado a la disputa y a su cuenta. En la regla actual del servicio, crear el
 * settlement tambien cambia la disputa a estado SETTLED.</p>
 */
@Entity
@Table(name = "dispute_settlements_t")
class DisputeSettlement {

    private static final DateTimeFormatter SETTLEMENT_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Id
    @Column(length = 19, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispute_id", nullable = false)
    private Dispute dispute;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(nullable = false, length = 240)
    private String note;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_t", updatable = false)
    private Instant createdT;

    @Column(name = "pin_virtual_time_t", updatable = false)
    private LocalDateTime pinVirtualTimeT;

    /**
     * Constructor requerido por JPA para materializar settlements persistidos.
     */
    protected DisputeSettlement() {
    }

    /**
     * Crea un settlement en COP para la disputa indicada.
     *
     * @param dispute disputa que se cierra por acuerdo.
     * @param amount monto acordado del settlement.
     * @param note nota obligatoria del acuerdo.
     */
    DisputeSettlement(Dispute dispute, BigDecimal amount, String note, LocalDateTime pinVirtualTimeT) {
        var now = Instant.now();
        this.id = newSettlementId();
        this.dispute = dispute;
        this.account = dispute.account();
        this.amount = amount;
        this.currency = Currency.COP;
        this.note = note;
        this.createdAt = now;
        this.createdT = now;
        this.pinVirtualTimeT = pinVirtualTimeT;
    }

    /**
     * Obtiene el identificador del settlement.
     *
     * @return id temporal del acuerdo.
     */
    String id() {
        return id;
    }

    /**
     * Obtiene la disputa asociada.
     *
     * @return disputa cerrada por el acuerdo.
     */
    Dispute dispute() {
        return dispute;
    }

    /**
     * Obtiene la cuenta asociada.
     *
     * @return cuenta de la disputa.
     */
    Account account() {
        return account;
    }

    /**
     * Obtiene el monto acordado.
     *
     * @return monto COP del settlement.
     */
    BigDecimal amount() {
        return amount;
    }

    /**
     * Obtiene la moneda del settlement.
     *
     * @return COP.
     */
    Currency currency() {
        return currency;
    }

    /**
     * Obtiene la nota del acuerdo.
     *
     * @return texto registrado al crear el settlement.
     */
    String note() {
        return note;
    }

    /**
     * Obtiene la fecha de creacion.
     *
     * @return instante de registro.
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

    private static String newSettlementId() {
        return LocalDateTime.now().format(SETTLEMENT_ID_FORMAT)
                + ThreadLocalRandom.current().nextInt(10, 100);
    }
}
