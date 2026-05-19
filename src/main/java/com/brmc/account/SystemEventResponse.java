package com.brmc.account;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para eventos de auditoria.
 *
 * @param id identificador del evento.
 * @param type tipo funcional.
 * @param entityType tipo logico de entidad afectada.
 * @param entityId identificador de la entidad afectada.
 * @param accountId cuenta relacionada, si aplica.
 * @param description descripcion del evento.
 * @param createdAt fecha de registro.
 */
record SystemEventResponse(
        String id,
        EventType type,
        String entityType,
        String entityId,
        String accountId,
        String description,
        Instant createdAt,
        Instant createdT,
        LocalDateTime pinVirtualTimeT
) {

    /**
     * Convierte un evento persistido en respuesta API.
     *
     * @param event evento de auditoria.
     * @return DTO serializable del evento.
     */
    static SystemEventResponse from(SystemEvent event) {
        return new SystemEventResponse(
                event.id(),
                event.type(),
                event.entityType(),
                event.entityId(),
                event.accountId(),
                event.description(),
                event.createdAt(),
                event.createdT(),
                event.pinVirtualTimeT()
        );
    }
}
