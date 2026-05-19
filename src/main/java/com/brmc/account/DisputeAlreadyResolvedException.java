package com.brmc.account;

/**
 * Excepcion usada cuando se intenta modificar una disputa que ya fue cerrada.
 */
class DisputeAlreadyResolvedException extends RuntimeException {

    /**
     * Crea la excepcion con el mensaje funcional que recibira el cliente.
     *
     * @param message detalle del intento de modificar una disputa cerrada.
     */
    DisputeAlreadyResolvedException(String message) {
        super(message);
    }
}
