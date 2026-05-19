package com.brmc.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Entidad JPA que representa un caso de disputa sobre una cuenta.
 *
 * <p>Las disputas nacen en estado PENDING y solo pueden resolverse una vez. La resolucion puede
 * aprobar, rechazar o cerrar por settlement; cualquier intento posterior lanza una excepcion de
 * negocio para conservar la trazabilidad del primer resultado.</p>
 */
@Entity
@Table(name = "disputes_t")
class Dispute {

    private static final DateTimeFormatter DISPUTE_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Id
    @Column(length = 19, nullable = false)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 240)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DisputeStatus status;

    @Column(length = 240)
    private String resolutionNote;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_t", updatable = false)
    private Instant createdT;

    @Column(name = "pin_virtual_time_t", updatable = false)
    private LocalDateTime pinVirtualTimeT;

    private Instant resolvedAt;

    @Column(name = "resolved_pin_virtual_time_t")
    private LocalDateTime resolvedPinVirtualTimeT;

    /**
     * Constructor requerido por JPA para materializar disputas persistidas.
     */
    protected Dispute() {
    }

    /**
     * Crea una disputa pendiente.
     *
     * @param account cuenta asociada al caso.
     * @param amount monto disputado en COP.
     * @param reason motivo operativo de la disputa.
     */
    Dispute(Account account, BigDecimal amount, String reason, LocalDateTime pinVirtualTimeT) {
        var now = Instant.now();
        this.id = newDisputeId();
        this.account = account;
        this.amount = amount;
        this.reason = reason;
        this.status = DisputeStatus.PENDING;
        this.createdAt = now;
        this.createdT = now;
        this.pinVirtualTimeT = pinVirtualTimeT;
    }

    /**
     * Obtiene el identificador de la disputa.
     *
     * @return id temporal del caso.
     */
    String id() {
        return id;
    }

    /**
     * Obtiene la cuenta asociada.
     *
     * @return cuenta del caso.
     */
    Account account() {
        return account;
    }

    /**
     * Obtiene el monto disputado.
     *
     * @return monto en COP.
     */
    BigDecimal amount() {
        return amount;
    }

    /**
     * Obtiene el motivo registrado.
     *
     * @return razon de la disputa.
     */
    String reason() {
        return reason;
    }

    /**
     * Obtiene el estado actual.
     *
     * @return PENDING, APPROVED, REJECTED o SETTLED.
     */
    DisputeStatus status() {
        return status;
    }

    /**
     * Obtiene la nota de resolucion.
     *
     * @return nota registrada al aprobar, rechazar o crear settlement.
     */
    String resolutionNote() {
        return resolutionNote;
    }

    /**
     * Obtiene la fecha de creacion.
     *
     * @return instante de apertura del caso.
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
     * Obtiene la fecha de resolucion.
     *
     * @return instante de cierre o {@code null} si sigue pendiente.
     */
    Instant resolvedAt() {
        return resolvedAt;
    }

    LocalDateTime resolvedPinVirtualTimeT() {
        return resolvedPinVirtualTimeT;
    }

    /**
     * Aprueba una disputa pendiente.
     *
     * @param note nota de resolucion.
     * @throws DisputeAlreadyResolvedException si la disputa ya fue cerrada.
     */
    void approve(String note, LocalDateTime pinVirtualTimeT) {
        resolve(DisputeStatus.APPROVED, note, pinVirtualTimeT);
    }

    /**
     * Rechaza una disputa pendiente.
     *
     * @param note nota de resolucion.
     * @throws DisputeAlreadyResolvedException si la disputa ya fue cerrada.
     */
    void reject(String note, LocalDateTime pinVirtualTimeT) {
        resolve(DisputeStatus.REJECTED, note, pinVirtualTimeT);
    }

    /**
     * Cierra una disputa pendiente mediante settlement.
     *
     * @param note nota asociada al settlement.
     * @throws DisputeAlreadyResolvedException si la disputa ya fue cerrada.
     */
    void settle(String note, LocalDateTime pinVirtualTimeT) {
        resolve(DisputeStatus.SETTLED, note, pinVirtualTimeT);
    }

    private void resolve(DisputeStatus newStatus, String note, LocalDateTime pinVirtualTimeT) {
        if (status != DisputeStatus.PENDING) {
            throw new DisputeAlreadyResolvedException("La disputa ya fue resuelta y no puede cambiarse.");
        }

        status = newStatus;
        resolutionNote = note;
        resolvedAt = Instant.now();
        resolvedPinVirtualTimeT = pinVirtualTimeT;
    }

    private static String newDisputeId() {
        return LocalDateTime.now().format(DISPUTE_ID_FORMAT)
                + ThreadLocalRandom.current().nextInt(10, 100);
    }
}
