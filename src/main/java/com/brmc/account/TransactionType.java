package com.brmc.account;

/**
 * Tipo de movimiento financiero registrado en el historial de cuenta.
 *
 * <p>Los pagos suman saldo. Reembolsos, write-off y cargos de billing reducen el saldo segun el
 * modelo financiero actual del proyecto.</p>
 */
enum TransactionType {
    /**
     * Pago recibido que incrementa el saldo.
     */
    PAYMENT,
    /**
     * Reembolso que reduce el saldo.
     */
    REFUND,
    /**
     * Ajuste write-off que reduce el saldo.
     */
    WRITE_OFF,
    /**
     * Cargo generado por billing que reduce el saldo.
     */
    BILLING_CHARGE
}
