package com.brmc.account;

/**
 * Excepcion general para reglas de negocio violadas por una solicitud valida a nivel sintactico.
 *
 * <p>Centraliza errores como productos duplicados, asignaciones no permitidas, estados invalidos
 * o combinaciones comerciales incompatibles.</p>
 */
class BusinessRuleException extends RuntimeException {

    /**
     * Crea la excepcion con el mensaje funcional que recibira el cliente.
     *
     * @param message detalle de la regla de negocio incumplida.
     */
    BusinessRuleException(String message) {
        super(message);
    }
}
