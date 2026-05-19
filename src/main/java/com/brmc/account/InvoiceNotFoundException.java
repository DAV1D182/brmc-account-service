package com.brmc.account;

/**
 * Excepcion emitida cuando una factura no existe.
 */
class InvoiceNotFoundException extends RuntimeException {

    /**
     * Crea la excepcion con el detalle funcional.
     *
     * @param message descripcion del invoice solicitado.
     */
    InvoiceNotFoundException(String message) {
        super(message);
    }
}
