package com.brmc.account;

/**
 * Metodos de pago CL aceptados por el modulo de pagos.
 *
 * <p>El metodo queda almacenado en pagos y transacciones para trazabilidad operativa. Si un pago
 * no especifica metodo, el servicio usa {@code CASH} como valor por defecto.</p>
 */
enum PaymentMethod {
    /**
     * Pago recibido en efectivo.
     */
    CASH,
    /**
     * Pago mediante cheque dia.
     */
    CHECK_DAY,
    /**
     * Pago con tarjeta debito.
     */
    DEBIT_CARD,
    /**
     * Pago con tarjeta credito.
     */
    CREDIT_CARD,
    /**
     * Pago manual con tarjeta debito.
     */
    MANUAL_DEBIT_CARD,
    /**
     * Pago manual con tarjeta credito.
     */
    MANUAL_CREDIT_CARD,
    /**
     * Pago por transferencia electronica.
     */
    ELECTRONIC_TRANSFER,
    /**
     * Ajuste por sencillo tratado como metodo de pago.
     */
    SIMPLE_ADJUSTMENT,
    /**
     * Pago por canje de puntos.
     */
    POINT_EXCHANGE
}
