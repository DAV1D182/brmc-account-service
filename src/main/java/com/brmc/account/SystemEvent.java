package com.brmc.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entidad JPA de auditoria funcional del sistema.
 *
 * <p>Registra eventos de cuentas, pagos, reembolsos, disputas, productos, servicios, billing y
 * fecha virtual en {@code system_events_t}. Los servicios de aplicacion crean estos eventos como
 * efecto secundario de operaciones relevantes para mantenimiento y trazabilidad.</p>
 */
@Entity
@Table(name = "system_events_t")
class SystemEvent {

    private static final DateTimeFormatter EVENT_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static String lastEventTimestamp;
    private static int eventSequence;

    @Id
    @Column(length = 19, nullable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EventType type;

    @Column(length = 40)
    private String entityType;

    @Column(length = 40)
    private String entityId;

    @Column(length = 14)
    private String accountId;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_t", updatable = false)
    private Instant createdT;

    @Column(name = "pin_virtual_time_t", updatable = false)
    private LocalDateTime pinVirtualTimeT;

    /**
     * Constructor requerido por JPA para materializar eventos persistidos.
     */
    protected SystemEvent() {
    }

    /**
     * Crea un evento de auditoria.
     *
     * @param type tipo funcional del evento.
     * @param entityType nombre logico de la entidad afectada.
     * @param entityId identificador de la entidad afectada.
     * @param accountId cuenta relacionada, si aplica.
     * @param description descripcion legible del cambio.
     */
    SystemEvent(EventType type, String entityType, String entityId, String accountId, String description, LocalDateTime pinVirtualTimeT) {
        var now = Instant.now();
        this.id = newEventId();
        this.type = type;
        this.entityType = entityType;
        this.entityId = entityId;
        this.accountId = accountId;
        this.description = description;
        this.createdAt = now;
        this.createdT = now;
        this.pinVirtualTimeT = pinVirtualTimeT;
    }

    /**
     * Obtiene el identificador del evento.
     *
     * @return id temporal del evento.
     */
    String id() {
        return id;
    }

    /**
     * Obtiene el tipo funcional.
     *
     * @return tipo de evento.
     */
    EventType type() {
        return type;
    }

    /**
     * Obtiene el tipo logico de entidad afectada.
     *
     * @return etiqueta como ACCOUNT, TRANSACTION, PRODUCT o BILLING_RUN.
     */
    String entityType() {
        return entityType;
    }

    /**
     * Obtiene el identificador de la entidad afectada.
     *
     * @return id de la entidad auditada.
     */
    String entityId() {
        return entityId;
    }

    /**
     * Obtiene la cuenta relacionada con el evento.
     *
     * @return id de cuenta o {@code null} para eventos globales.
     */
    String accountId() {
        return accountId;
    }

    /**
     * Obtiene la descripcion funcional del evento.
     *
     * @return mensaje de auditoria.
     */
    String description() {
        return description;
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

    private static synchronized String newEventId() {
        var timestamp = LocalDateTime.now().format(EVENT_ID_FORMAT);
        if (!timestamp.equals(lastEventTimestamp)) {
            lastEventTimestamp = timestamp;
            eventSequence = 0;
        } else {
            eventSequence++;
            if (eventSequence > 99) {
                do {
                    timestamp = LocalDateTime.now().format(EVENT_ID_FORMAT);
                } while (timestamp.equals(lastEventTimestamp));
                lastEventTimestamp = timestamp;
                eventSequence = 0;
            }
        }
        return timestamp + String.format("%02d", eventSequence);
    }
}
