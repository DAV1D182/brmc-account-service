package com.brmc.account;

/**
 * Excepcion para reembolsos no permitidos sobre un pago.
 *
 * <p>En el modelo actual los reembolsos son siempre por la totalidad del pago origen, por lo que
 * esta excepcion se usa principalmente para impedir reembolsos duplicados.</p>
 */
class RefundAmountExceededException extends RuntimeException {

    /**
     * Crea la excepcion con el mensaje funcional que recibira el cliente.
     *
     * @param message detalle del reembolso no permitido.
     */
    RefundAmountExceededException(String message) {
        super(message);
    }
}
