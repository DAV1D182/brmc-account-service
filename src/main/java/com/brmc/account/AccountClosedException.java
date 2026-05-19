package com.brmc.account;

/**
 * Excepcion de negocio para operaciones solicitadas sobre cuentas cerradas.
 *
 * <p>Se traduce a HTTP 409 y protege operaciones financieras, servicios y billing que requieren
 * una cuenta en estado {@code ACTIVE}.</p>
 */
class AccountClosedException extends RuntimeException {

    /**
     * Crea la excepcion con el mensaje funcional que recibira el cliente.
     *
     * @param message detalle de la operacion bloqueada.
     */
    AccountClosedException(String message) {
        super(message);
    }
}
