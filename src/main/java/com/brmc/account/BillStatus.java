package com.brmc.account;

/**
 * Estado financiero basico de un bill generado por billing.
 *
 * <p>El proyecto crea bills en estado OPEN porque todavia no existe aplicacion de pagos contra
 * bills. Los estados PAID y CLOSED quedan disponibles para extender el flujo de cobranza.</p>
 */
enum BillStatus {
    /**
     * Bill con saldo pendiente o sin proceso de pago aplicado.
     */
    OPEN,
    /**
     * Bill completamente pagado.
     */
    PAID,
    /**
     * Bill cerrado logicamente.
     */
    CLOSED
}
