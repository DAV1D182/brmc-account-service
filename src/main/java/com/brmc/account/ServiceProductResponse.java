package com.brmc.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para productos asignados a servicios.
 *
 * @param id identificador de la asignacion.
 * @param serviceId servicio propietario.
 * @param productId producto asignado.
 * @param productCode codigo funcional del producto.
 * @param productName nombre del producto.
 * @param productType tipo de cobro del producto.
 * @param price precio usado por billing.
 * @param currency moneda del precio.
 * @param billingFrequency frecuencia de cobro.
 * @param status estado de la asignacion.
 * @param assignedAt fecha virtual de asignacion.
 * @param cancelledAt fecha virtual de cancelacion, si existe.
 * @param lastBilledAt ultima fecha virtual facturada.
 * @param nextBillAt proxima fecha virtual elegible para billing.
 */
record ServiceProductResponse(
        String id,
        String serviceId,
        String productId,
        String productCode,
        String productName,
        ProductType productType,
        BigDecimal price,
        Currency currency,
        BillingFrequency billingFrequency,
        ServiceProductStatus status,
        LocalDateTime assignedAt,
        Instant createdT,
        LocalDateTime pinVirtualTimeT,
        LocalDateTime cancelledAt,
        LocalDateTime lastBilledAt,
        LocalDateTime nextBillAt
) {

    /**
     * Convierte una asignacion servicio-producto en DTO.
     *
     * @param serviceProduct asignacion persistida.
     * @return respuesta con datos del producto y fechas de billing.
     */
    static ServiceProductResponse from(ServiceProduct serviceProduct) {
        var product = serviceProduct.product();
        return new ServiceProductResponse(
                serviceProduct.id(),
                serviceProduct.service().id(),
                product.id(),
                product.code(),
                product.name(),
                product.productType(),
                product.price(),
                product.currency(),
                product.billingFrequency(),
                serviceProduct.status(),
                serviceProduct.assignedAt(),
                serviceProduct.createdT(),
                serviceProduct.pinVirtualTimeT(),
                serviceProduct.cancelledAt(),
                serviceProduct.lastBilledAt(),
                serviceProduct.nextBillAt()
        );
    }
}
