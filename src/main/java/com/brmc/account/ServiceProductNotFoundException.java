package com.brmc.account;

/**
 * Excepcion usada cuando una asignacion producto-servicio no existe.
 */
class ServiceProductNotFoundException extends RuntimeException {

    /**
     * Crea la excepcion con el mensaje funcional que recibira el cliente.
     *
     * @param message detalle de la asignacion no encontrada.
     */
    ServiceProductNotFoundException(String message) {
        super(message);
    }
}
