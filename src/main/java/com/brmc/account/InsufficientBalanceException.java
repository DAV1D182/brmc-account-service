package com.brmc.account;

/**
 * Excepcion para operaciones que requieren saldo disponible y no pueden completarse.
 *
 * <p>Actualmente protege reembolsos y write-off. Los cargos de billing no usan esta excepcion
 * porque pueden dejar saldo negativo como deuda pendiente.</p>
 */
class InsufficientBalanceException extends RuntimeException {

    /**
     * Crea la excepcion con el mensaje funcional que recibira el cliente.
     *
     * @param message detalle de la insuficiencia de saldo.
     */
    InsufficientBalanceException(String message) {
        super(message);
    }
}
