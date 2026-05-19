package com.brmc.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para exponer una cuenta por API.
 *
 * @param id identificador funcional de la cuenta.
 * @param ownerName nombre del titular.
 * @param phoneNumber numero de contacto.
 * @param email correo de contacto.
 * @param balance saldo neto actual en COP.
 * @param currency moneda base de la cuenta.
 * @param status estado operativo.
 * @param createdAt fecha de creacion.
 */
record AccountResponse(
        String id,
        String ownerName,
        String phoneNumber,
        String email,
        BigDecimal balance,
        Integer billingDom,
        String billingCycle,
        String billNo,
        String ownerUsername,
        Currency currency,
        AccountStatus status,
        Instant createdAt,
        Instant createdT,
        LocalDateTime pinVirtualTimeT
) {

    /**
     * Convierte una entidad de dominio en DTO de respuesta.
     *
     * @param account cuenta persistida.
     * @return representacion serializable para API y UI.
     */
    static AccountResponse from(Account account) {
        return new AccountResponse(
                account.id(),
                account.ownerName(),
                account.phoneNumber(),
                account.email(),
                account.balance(),
                account.billingDom(),
                account.billingCycle(),
                account.billNo(),
                account.ownerUsername(),
                account.currency(),
                account.status(),
                account.createdAt(),
                account.createdT(),
                account.pinVirtualTimeT()
        );
    }
}
