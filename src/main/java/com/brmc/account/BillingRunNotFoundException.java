package com.brmc.account;

/**
 * Excepcion usada cuando no se encuentra una corrida de billing por identificador.
 */
class BillingRunNotFoundException extends RuntimeException {

    /**
     * Crea la excepcion con el mensaje funcional que recibira el cliente.
     *
     * @param message detalle de la corrida no encontrada.
     */
    BillingRunNotFoundException(String message) {
        super(message);
    }
}
