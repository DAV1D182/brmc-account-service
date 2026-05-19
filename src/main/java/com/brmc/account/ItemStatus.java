package com.brmc.account;

/**
 * Estado contable de un item dentro de un bill.
 *
 * <p>Los items nacen OPEN porque representan cargos pendientes de aplicacion de pago. Los estados
 * PAID y DISPUTED permiten evolucionar el modelo hacia cobranza y disputas por item.</p>
 */
enum ItemStatus {
    /**
     * Item pendiente de pago o aplicacion.
     */
    OPEN,
    /**
     * Item cubierto por pagos aplicados.
     */
    PAID,
    /**
     * Item marcado para investigacion o disputa.
     */
    DISPUTED
}
