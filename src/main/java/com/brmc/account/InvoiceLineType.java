package com.brmc.account;

/**
 * Clasificacion de lineas dentro de una factura.
 */
enum InvoiceLineType {
    /**
     * Cargo unico de activacion o compra puntual.
     */
    ONE_TIME,
    /**
     * Cargo recurrente de ciclo mensual.
     */
    RECURRING,
    /**
     * Ajuste manual o compensacion.
     */
    ADJUSTMENT,
    /**
     * Linea de impuesto.
     */
    TAX,
    /**
     * Otro concepto no clasificado.
     */
    OTHER
}
