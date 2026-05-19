package com.brmc.account;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para definiciones generales de servicio.
 *
 * @param id identificador visible del catalogo.
 * @param name nombre funcional.
 * @param serviceType tipo de servicio.
 * @param description descripcion opcional.
 * @param status estado de disponibilidad.
 * @param createdAt fecha virtual de creacion.
 * @param createdT fecha real de creacion.
 * @param pinVirtualTimeT fecha virtual registrada al crear la definicion.
 * @param updatedAt fecha virtual de ultima actualizacion.
 */
record ServiceCatalogResponse(
        String id,
        String name,
        ServiceType serviceType,
        String description,
        ServiceCatalogStatus status,
        LocalDateTime createdAt,
        Instant createdT,
        LocalDateTime pinVirtualTimeT,
        LocalDateTime updatedAt
) {

    static ServiceCatalogResponse from(ServiceCatalog catalog) {
        return new ServiceCatalogResponse(
                catalog.id(),
                catalog.name(),
                catalog.serviceType(),
                catalog.description(),
                catalog.status(),
                catalog.createdAt(),
                catalog.createdT(),
                catalog.pinVirtualTimeT(),
                catalog.updatedAt()
        );
    }
}
