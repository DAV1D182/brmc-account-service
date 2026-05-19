package com.brmc.account;

/**
 * Excepcion usada cuando una cuenta solicitada no existe en {@code accounts_t}.
 */
class AccountNotFoundException extends RuntimeException {

    /**
     * Crea la excepcion con el mensaje funcional que recibira el cliente.
     *
     * @param message detalle de la cuenta no encontrada.
     */
    AccountNotFoundException(String message) {
        super(message);
    }
}
