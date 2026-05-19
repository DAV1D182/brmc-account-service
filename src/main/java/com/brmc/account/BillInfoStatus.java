package com.brmc.account;

/**
 * Estado operativo de la configuracion de facturacion de una cuenta.
 *
 * <p>Permite distinguir configuraciones vigentes de configuraciones cerradas sin eliminar el
 * historial asociado a bills, items y eventos de auditoria.</p>
 */
enum BillInfoStatus {
    /**
     * Configuracion disponible para generar bills.
     */
    ACTIVE,
    /**
     * Configuracion cerrada para futuras ejecuciones de billing.
     */
    CLOSED
}
