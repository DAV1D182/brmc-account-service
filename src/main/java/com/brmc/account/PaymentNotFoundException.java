package com.brmc.account;

/**
 * Excepcion usada cuando un pago origen requerido por un reembolso no existe.
 */
class PaymentNotFoundException extends RuntimeException {

    /**
     * Crea la excepcion con el mensaje funcional que recibira el cliente.
     *
     * @param message detalle del pago no encontrado.
     */
    PaymentNotFoundException(String message) {
        super(message);
    }
}
