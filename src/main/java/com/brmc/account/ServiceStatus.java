package com.brmc.account;

/**
 * Estado operativo de un servicio asociado a una cuenta.
 *
 * <p>Solo servicios {@code ACTIVE} pueden facturarse. Los servicios suspendidos no generan cargos
 * y los terminados no aceptan nuevos productos.</p>
 */
enum ServiceStatus {
    /**
     * Servicio vigente y elegible para billing.
     */
    ACTIVE,
    /**
     * Servicio pausado temporalmente y excluido de billing.
     */
    SUSPENDED,
    /**
     * Servicio cerrado definitivamente.
     */
    TERMINATED
}
