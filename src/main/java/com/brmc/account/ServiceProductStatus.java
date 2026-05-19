package com.brmc.account;

/**
 * Estado de la relacion entre un servicio y un producto asignado.
 *
 * <p>Billing solo evalua asignaciones {@code ACTIVE}. Las asignaciones {@code CANCELLED} se
 * conservan como historial y no generan nuevos cargos.</p>
 */
enum ServiceProductStatus {
    /**
     * Asignacion vigente y elegible para billing.
     */
    ACTIVE,
    /**
     * Asignacion cancelada y conservada como historial.
     */
    CANCELLED
}
