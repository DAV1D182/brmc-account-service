package com.brmc.account;

/**
 * Excepcion usada cuando una disputa solicitada no existe.
 */
class DisputeNotFoundException extends RuntimeException {

    /**
     * Crea la excepcion con el mensaje funcional que recibira el cliente.
     *
     * @param message detalle de la disputa no encontrada.
     */
    DisputeNotFoundException(String message) {
        super(message);
    }
}
