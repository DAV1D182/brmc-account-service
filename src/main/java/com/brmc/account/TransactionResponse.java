package com.brmc.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para transacciones de una cuenta especifica.
 *
 * @param id identificador de la transaccion.
 * @param type tipo de movimiento financiero.
 * @param amount monto que impacto el saldo en COP.
 * @param currency moneda contable del movimiento.
 * @param originalAmount monto original informado por la operacion.
 * @param originalCurrency moneda original de la operacion.
 * @param exchangeRate tasa aplicada para convertir a COP.
 * @param paymentMethod metodo de pago, cuando aplica.
 * @param description descripcion operativa.
 * @param createdAt fecha de creacion.
 */
record TransactionResponse(
        String id,
        TransactionType type,
        BigDecimal amount,
        Currency currency,
        BigDecimal originalAmount,
        Currency originalCurrency,
        BigDecimal exchangeRate,
        PaymentMethod paymentMethod,
        String description,
        Instant createdAt,
        Instant createdT,
        LocalDateTime pinVirtualTimeT
) {

    /**
     * Convierte una transaccion de dominio en respuesta API.
     *
     * @param transaction transaccion persistida.
     * @return DTO con datos contables y de conversion.
     */
    static TransactionResponse from(AccountTransaction transaction) {
        return new TransactionResponse(
                transaction.id(),
                transaction.type(),
                transaction.amount(),
                transaction.currency(),
                transaction.originalAmount(),
                transaction.originalCurrency(),
                transaction.exchangeRate(),
                transaction.paymentMethod(),
                transaction.description(),
                transaction.createdAt(),
                transaction.createdT(),
                transaction.pinVirtualTimeT()
        );
    }
}
