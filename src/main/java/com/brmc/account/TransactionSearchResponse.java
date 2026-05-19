package com.brmc.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * DTO usado por busquedas globales y exportacion CSV de transacciones.
 *
 * @param accountId cuenta propietaria del movimiento.
 * @param ownerName titular de la cuenta.
 * @param id identificador de la transaccion.
 * @param type tipo de movimiento.
 * @param amount monto contable en COP.
 * @param currency moneda contable.
 * @param originalAmount monto original.
 * @param originalCurrency moneda original.
 * @param exchangeRate tasa de conversion a COP.
 * @param paymentMethod metodo de pago cuando el movimiento es PAYMENT.
 * @param description descripcion operativa.
 * @param createdAt fecha de creacion.
 */
record TransactionSearchResponse(
        String accountId,
        String ownerName,
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
     * Construye una respuesta de busqueda combinando cuenta y transaccion.
     *
     * @param account cuenta propietaria.
     * @param transaction transaccion encontrada.
     * @return DTO enriquecido con datos del titular.
     */
    static TransactionSearchResponse from(Account account, AccountTransaction transaction) {
        return new TransactionSearchResponse(
                account.id(),
                account.ownerName(),
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
