package com.brmc.account;

/**
 * Monedas soportadas por el dominio financiero.
 *
 * <p>La moneda base de cuenta y transacciones persistidas es {@code COP}. {@code USD} se acepta
 * para pagos y se convierte a COP mediante el servicio de TRM.</p>
 */
enum Currency {
    /**
     * Peso colombiano, moneda base del sistema.
     */
    COP,
    /**
     * Dolar estadounidense aceptado para pagos con conversion a COP.
     */
    USD
}
