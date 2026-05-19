package com.brmc.account;

/**
 * Excepcion usada cuando un servicio BRM no existe o no puede recuperarse.
 */
class BrmServiceNotFoundException extends RuntimeException {

    /**
     * Crea la excepcion con el mensaje funcional que recibira el cliente.
     *
     * @param message detalle del servicio no encontrado.
     */
    BrmServiceNotFoundException(String message) {
        super(message);
    }
}
