package com.brmc.account;

/**
 * Estado de resolucion de una disputa.
 *
 * <p>Una disputa inicia en {@code PENDING}. Puede cerrarse por aprobacion, rechazo o por
 * settlement; una vez cerrada no admite nuevas acciones de resolucion.</p>
 */
enum DisputeStatus {
    /**
     * Disputa abierta y pendiente de decision.
     */
    PENDING,
    /**
     * Disputa aprobada por el operador.
     */
    APPROVED,
    /**
     * Disputa rechazada por el operador.
     */
    REJECTED,
    /**
     * Disputa cerrada mediante settlement.
     */
    SETTLED
}
