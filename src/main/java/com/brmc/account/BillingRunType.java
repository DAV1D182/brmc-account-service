package com.brmc.account;

/**
 * Alcance con el que se ejecuta un proceso de billing.
 *
 * <p>{@code MANUAL} procesa todas las cuentas activas. {@code ACCOUNT} procesa una cuenta
 * especifica solicitada desde API o UI.</p>
 */
enum BillingRunType {
    /**
     * Ejecucion general sobre todas las cuentas activas.
     */
    MANUAL,
    /**
     * Ejecucion limitada a una cuenta especifica.
     */
    ACCOUNT
}
