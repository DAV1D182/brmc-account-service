package com.brmc.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para items de inventario.
 *
 * @param id identificador de inventario.
 * @param accountId cuenta propietaria del item.
 * @param accountNumber numero visible de la cuenta propietaria.
 * @param accountOwnerName titular de la cuenta propietaria.
 * @param name nombre.
 * @param description descripcion.
 * @param unitPrice precio por unidad.
 * @param stockQuantity cantidad en existencias.
 * @param inventoryValue valor total del inventario.
 * @param reorderLevel nivel del nuevo pedido.
 * @param reorderTimeDays tiempo del nuevo pedido en dias.
 * @param reorderQuantity cantidad del nuevo pedido.
 * @param available indica si aun se encuentra disponible.
 * @param needsReorder indica si la existencia llego al nivel de reorden.
 * @param createdAt fecha real de creacion.
 * @param createdT reloj real tecnico.
 * @param pinVirtualTimeT fecha virtual de negocio.
 * @param updatedAt fecha real de actualizacion.
 * @param updatedPinVirtualTimeT fecha virtual de actualizacion.
 */
record InventoryItemResponse(
        String id,
        String accountId,
        String accountNumber,
        String accountOwnerName,
        String name,
        String description,
        BigDecimal unitPrice,
        Integer stockQuantity,
        BigDecimal inventoryValue,
        Integer reorderLevel,
        Integer reorderTimeDays,
        Integer reorderQuantity,
        Boolean available,
        Boolean needsReorder,
        Instant createdAt,
        Instant createdT,
        LocalDateTime pinVirtualTimeT,
        Instant updatedAt,
        LocalDateTime updatedPinVirtualTimeT
) {

    /**
     * Convierte un item de inventario en respuesta API.
     *
     * @param item item persistido.
     * @return DTO serializable.
     */
    static InventoryItemResponse from(InventoryItem item) {
        var account = item.account();
        return new InventoryItemResponse(
                item.id(),
                account == null ? null : account.id(),
                account == null ? "GLOBAL" : account.id(),
                account == null ? "Inventario global" : account.ownerName(),
                item.name(),
                item.description(),
                item.unitPrice(),
                item.stockQuantity(),
                item.inventoryValue(),
                item.reorderLevel(),
                item.reorderTimeDays(),
                item.reorderQuantity(),
                item.available(),
                item.needsReorder(),
                item.createdAt(),
                item.createdT(),
                item.pinVirtualTimeT(),
                item.updatedAt(),
                item.updatedPinVirtualTimeT()
        );
    }
}
