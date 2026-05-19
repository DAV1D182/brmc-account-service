package com.brmc.account;

/**
 * Excepcion usada cuando un producto del catalogo no existe.
 */
class ProductNotFoundException extends RuntimeException {

    /**
     * Crea la excepcion con el mensaje funcional que recibira el cliente.
     *
     * @param message detalle del producto no encontrado.
     */
    ProductNotFoundException(String message) {
        super(message);
    }
}
