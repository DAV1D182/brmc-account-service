package com.brmc.account;

/**
 * Estados funcionales de una factura de billing.
 *
 * <p>El estado controla las acciones permitidas sobre {@code invoices_t}: una factura cancelada no
 * acepta pagos y una factura pagada no puede cancelarse.</p>
 */
enum InvoiceStatus {
    /**
     * Factura preparada pero no emitida formalmente.
     */
    DRAFT,
    /**
     * Factura generada por billing y disponible para consulta.
     */
    ISSUED,
    /**
     * Factura marcada como enviada al cliente.
     */
    SENT,
    /**
     * Factura con pago parcial aplicado.
     */
    PARTIALLY_PAID,
    /**
     * Factura totalmente pagada.
     */
    PAID,
    /**
     * Factura con una nota de credito parcial aplicada.
     */
    PARTIALLY_CREDITED,
    /**
     * Factura cubierta completamente por una o mas notas de credito.
     */
    CREDITED,
    /**
     * Factura anulada; no permite pagos posteriores.
     */
    CANCELLED
}
