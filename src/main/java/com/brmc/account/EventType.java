package com.brmc.account;

/**
 * Catalogo de eventos auditables del sistema.
 *
 * <p>Cada valor representa una accion de negocio registrada en {@code system_events_t}. La lista
 * se mantiene sincronizada con las restricciones CHECK de PostgreSQL para garantizar integridad
 * entre codigo y base de datos.</p>
 */
enum EventType {
    /**
     * Cuenta creada correctamente.
     */
    ACCOUNT_CREATED,
    /**
     * Pago recibido sobre una cuenta.
     */
    PAYMENT_RECEIVED,
    /**
     * Pago recibido sin asignacion a bill o item.
     */
    UNALLOCATED_PAYMENT_CREATED,
    /**
     * Reembolso registrado desde un pago origen.
     */
    REFUND_SENT,
    /**
     * Write-off aplicado sobre una cuenta.
     */
    WRITE_OFF_APPLIED,
    /**
     * Cuenta cerrada logicamente.
     */
    ACCOUNT_CLOSED,
    /**
     * Disputa creada en estado pendiente.
     */
    DISPUTE_CREATED,
    /**
     * Settlement creado para una disputa.
     */
    DISPUTE_SETTLEMENT_CREATED,
    /**
     * Disputa cerrada por settlement.
     */
    DISPUTE_SETTLED,
    /**
     * Disputa aprobada.
     */
    DISPUTE_APPROVED,
    /**
     * Disputa rechazada.
     */
    DISPUTE_REJECTED,
    /**
     * Fecha virtual actualizada manualmente.
     */
    VIRTUAL_TIME_UPDATED,
    /**
     * Fecha virtual reiniciada al reloj real.
     */
    VIRTUAL_TIME_RESET,
    /**
     * Producto creado en el catalogo.
     */
    PRODUCT_CREATED,
    /**
     * Producto actualizado en el catalogo.
     */
    PRODUCT_UPDATED,
    /**
     * Producto activado.
     */
    PRODUCT_ACTIVATED,
    /**
     * Producto desactivado.
     */
    PRODUCT_DEACTIVATED,
    /**
     * Definicion general de servicio creada en el catalogo.
     */
    SERVICE_CATALOG_CREATED,
    /**
     * Definicion general de servicio actualizada.
     */
    SERVICE_CATALOG_UPDATED,
    /**
     * Definicion general de servicio activada en una cuenta.
     */
    SERVICE_ACTIVATED,
    /**
     * Servicio creado para una cuenta.
     */
    SERVICE_CREATED,
    /**
     * Servicio actualizado.
     */
    SERVICE_UPDATED,
    /**
     * Servicio suspendido.
     */
    SERVICE_SUSPENDED,
    /**
     * Servicio reactivado.
     */
    SERVICE_REACTIVATED,
    /**
     * Servicio terminado.
     */
    SERVICE_TERMINATED,
    /**
     * Producto asignado a un servicio.
     */
    SERVICE_PRODUCT_ASSIGNED,
    /**
     * Producto cancelado de un servicio.
     */
    SERVICE_PRODUCT_CANCELLED,
    /**
     * Corrida de billing iniciada.
     */
    BILLING_RUN_STARTED,
    /**
     * Corrida de billing completada.
     */
    BILLING_RUN_COMPLETED,
    /**
     * Corrida de billing fallida.
     */
    BILLING_RUN_FAILED,
    /**
     * Cargo de billing creado.
     */
    BILLING_CHARGE_CREATED,
    /**
     * Configuracion de billinfo creada para una cuenta.
     */
    BILLINFO_CREATED,
    /**
     * Bill creado para agrupar items de una cuenta.
     */
    BILL_CREATED,
    /**
     * Item creado desde un cargo de billing.
     */
    ITEM_CREATED,
    /**
     * Bill item creado desde un cargo de billing.
     */
    BILL_ITEM_CREATED,
    /**
     * Item de inventario creado.
     */
    INVENTORY_ITEM_CREATED,
    /**
     * Item de inventario actualizado.
     */
    INVENTORY_ITEM_UPDATED,
    /**
     * Disponibilidad de un item de inventario modificada.
     */
    INVENTORY_AVAILABILITY_CHANGED,
    /**
     * Invoice generada desde una corrida de billing.
     */
    INVOICE_GENERATED,
    /**
     * Linea de invoice creada desde un cargo.
     */
    INVOICE_LINE_CREATED,
    /**
     * Invoice marcada como enviada.
     */
    INVOICE_SENT,
    /**
     * Invoice cancelada.
     */
    INVOICE_CANCELLED,
    /**
     * Invoice pagada totalmente.
     */
    INVOICE_PAID,
    /**
     * Invoice con pago parcial aplicado.
     */
    INVOICE_PARTIALLY_PAID,
    /**
     * Nota de credito creada para una invoice.
     */
    CREDIT_NOTE_CREATED,
    /**
     * Linea de nota de credito creada.
     */
    CREDIT_NOTE_LINE_CREATED,
    /**
     * Nota de credito aplicada al saldo de la invoice.
     */
    CREDIT_NOTE_APPLIED,
    /**
     * Nota de credito cancelada y reversada.
     */
    CREDIT_NOTE_CANCELLED
}
