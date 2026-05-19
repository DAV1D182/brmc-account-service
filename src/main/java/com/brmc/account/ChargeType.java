package com.brmc.account;

/**
 * Tipo de cargo creado por billing.
 *
 * <p>Se deriva del tipo de producto: {@code ONE_TIME} se factura una sola vez por asignacion de
 * producto; {@code RECURRING} se factura segun el calendario mensual.</p>
 */
enum ChargeType {
    /**
     * Cargo generado una sola vez por una asignacion de producto.
     */
    ONE_TIME,
    /**
     * Cargo generado periodicamente por un producto recurrente.
     */
    RECURRING
}
