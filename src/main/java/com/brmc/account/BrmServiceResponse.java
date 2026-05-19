package com.brmc.account;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.List;

/**
 * DTO de respuesta para servicios asociados a una cuenta.
 *
 * @param id identificador interno del servicio.
 * @param accountId cuenta propietaria.
 * @param ownerName titular de la cuenta.
 * @param serviceName nombre visible del servicio.
 * @param serviceCode codigo funcional del servicio.
 * @param catalogServiceId definicion general usada para activarlo.
 * @param serviceType tipo de servicio contratado.
 * @param status estado operativo.
 * @param activationDate fecha virtual de activacion.
 * @param terminationDate fecha virtual de terminacion, si existe.
 * @param createdAt fecha virtual de creacion.
 * @param updatedAt fecha virtual de ultima actualizacion.
 * @param products productos asignados al servicio.
 */
record BrmServiceResponse(
        String id,
        String accountId,
        String ownerName,
        String serviceName,
        String serviceCode,
        String catalogServiceId,
        ServiceType serviceType,
        ServiceStatus status,
        LocalDateTime activationDate,
        LocalDateTime terminationDate,
        LocalDateTime createdAt,
        Instant createdT,
        LocalDateTime pinVirtualTimeT,
        LocalDateTime updatedAt,
        List<ServiceProductResponse> products
) {

    /**
     * Construye la respuesta del servicio incluyendo sus productos asignados.
     *
     * @param service servicio persistido.
     * @param products asignaciones recuperadas para el servicio.
     * @return DTO de servicio con productos anidados.
     */
    static BrmServiceResponse from(BrmService service, List<ServiceProduct> products) {
        return new BrmServiceResponse(
                service.id(),
                service.account().id(),
                service.account().ownerName(),
                service.serviceName(),
                service.serviceCode(),
                service.catalogServiceId(),
                service.serviceType(),
                service.status(),
                service.activationDate(),
                service.terminationDate(),
                service.createdAt(),
                service.createdT(),
                service.pinVirtualTimeT(),
                service.updatedAt(),
                products.stream().map(ServiceProductResponse::from).toList()
        );
    }
}
