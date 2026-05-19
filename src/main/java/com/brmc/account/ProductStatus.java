package com.brmc.account;

/**
 * Estado comercial de un producto del catalogo.
 *
 * <p>Solo productos {@code ACTIVE} pueden asignarse a servicios y participar en billing.</p>
 */
enum ProductStatus {
    /**
     * Producto disponible para asignarse y facturarse.
     */
    ACTIVE,
    /**
     * Producto no disponible para nuevas asignaciones.
     */
    INACTIVE
}
