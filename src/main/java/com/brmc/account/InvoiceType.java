package com.brmc.account;

/**
 * Tipo de factura generado por el modulo de invoices.
 */
enum InvoiceType {
    /**
     * Factura con totales consolidados.
     */
    SUMMARY,
    /**
     * Factura con una linea por cada cargo de billing.
     */
    DETAILED
}
