package com.brmc.account;

/**
 * Frecuencia con la que un producto debe ser considerado por el proceso de billing.
 *
 * <p>{@code NONE} se usa para cargos unicos. {@code MONTHLY} representa cargos recurrentes
 * mensuales evaluados contra la fecha virtual del sistema.</p>
 */
enum BillingFrequency {
    /**
     * Sin recurrencia; se usa para productos de cobro unico.
     */
    NONE,
    /**
     * Frecuencia mensual evaluada contra la fecha virtual.
     */
    MONTHLY
}
