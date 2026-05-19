package com.brmc.account;

/**
 * Estado de asignacion de un pago frente a bills o items.
 *
 * <p>En esta fase los pagos nuevos quedan UNALLOCATED: aumentan el saldo de la cuenta, pero no se
 * aplican automaticamente a un bill especifico. Esto refleja el concepto BRM de pagos recibidos
 * pendientes de asignacion.</p>
 */
enum PaymentAllocationStatus {
    /**
     * Todo el monto del pago esta pendiente de asignacion.
     */
    UNALLOCATED,
    /**
     * Parte del pago fue aplicado y queda remanente disponible.
     */
    PARTIALLY_ALLOCATED,
    /**
     * El pago fue aplicado completamente.
     */
    ALLOCATED
}
