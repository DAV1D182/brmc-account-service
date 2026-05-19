package com.brmc.account;

/**
 * Estado operativo de una cuenta BRMC.
 *
 * <p>El estado controla si la cuenta puede recibir operaciones financieras y de servicios. Las
 * cuentas cerradas se conservan para auditoria, pero las reglas de negocio impiden pagos,
 * reembolsos, write-off, servicios y billing sobre ellas.</p>
 */
enum AccountStatus {
    /**
     * Cuenta habilitada para operaciones financieras, servicios y billing.
     */
    ACTIVE,
    /**
     * Cuenta cerrada logicamente y conservada solo para consulta y auditoria.
     */
    CLOSED
}
