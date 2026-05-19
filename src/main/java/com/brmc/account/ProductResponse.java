package com.brmc.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para productos del catalogo.
 *
 * @param id identificador interno del producto.
 * @param displayId identificador visible corto.
 * @param accountId cuenta propietaria del producto.
 * @param accountNumber numero visible de la cuenta propietaria.
 * @param accountOwnerName titular de la cuenta propietaria.
 * @param code codigo funcional unico.
 * @param name nombre comercial.
 * @param description descripcion opcional.
 * @param productType tipo de cobro.
 * @param price precio configurado.
 * @param currency moneda del precio.
 * @param billingFrequency frecuencia de billing.
 * @param status estado comercial.
 * @param createdAt fecha de creacion.
 * @param updatedAt fecha de ultima actualizacion.
 */
record ProductResponse(
        String id,
        String displayId,
        String accountId,
        String accountNumber,
        String accountOwnerName,
        String code,
        String name,
        String description,
        ProductType productType,
        BigDecimal price,
        Currency currency,
        BillingFrequency billingFrequency,
        ProductStatus status,
        Instant createdAt,
        Instant createdT,
        LocalDateTime pinVirtualTimeT,
        Instant updatedAt,
        LocalDateTime updatedPinVirtualTimeT
) {

    /**
     * Convierte un producto persistido en DTO de catalogo.
     *
     * @param product producto de dominio.
     * @return respuesta serializable para API.
     */
    static ProductResponse from(Product product) {
        var account = product.account();
        return new ProductResponse(
                product.id(),
                product.displayId(),
                account == null ? null : account.id(),
                account == null ? "GLOBAL" : account.id(),
                account == null ? "Catalogo global" : account.ownerName(),
                product.code(),
                product.name(),
                product.description(),
                product.productType(),
                product.price(),
                product.currency(),
                product.billingFrequency(),
                product.status(),
                product.createdAt(),
                product.createdT(),
                product.pinVirtualTimeT(),
                product.updatedAt(),
                product.updatedPinVirtualTimeT()
        );
    }
}
