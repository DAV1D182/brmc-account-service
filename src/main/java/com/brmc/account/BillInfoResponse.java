package com.brmc.account;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para la configuracion de facturacion de una cuenta.
 *
 * @param id identificador del billinfo.
 * @param accountId cuenta propietaria.
 * @param billInfoNo numero funcional.
 * @param billingDom dia de facturacion.
 * @param billingCycle ciclo configurado.
 * @param currency moneda de facturacion.
 * @param status estado operativo.
 * @param lastBillAt ultima fecha virtual facturada.
 * @param nextBillAt proxima fecha virtual estimada.
 * @param createdAt fecha real de creacion.
 * @param createdT reloj real tecnico.
 * @param pinVirtualTimeT fecha virtual de negocio.
 */
record BillInfoResponse(
        String id,
        String accountId,
        String billInfoNo,
        Integer billingDom,
        String billingCycle,
        Currency currency,
        BillInfoStatus status,
        LocalDateTime lastBillAt,
        LocalDateTime nextBillAt,
        Instant createdAt,
        Instant createdT,
        LocalDateTime pinVirtualTimeT
) {

    /**
     * Convierte un billinfo persistido en respuesta API.
     *
     * @param billInfo configuracion persistida.
     * @return DTO de billinfo.
     */
    static BillInfoResponse from(BillInfo billInfo) {
        return new BillInfoResponse(
                billInfo.id(),
                billInfo.account().id(),
                billInfo.billInfoNo(),
                billInfo.billingDom(),
                billInfo.billingCycle(),
                billInfo.currency(),
                billInfo.status(),
                billInfo.lastBillAt(),
                billInfo.nextBillAt(),
                billInfo.createdAt(),
                billInfo.createdT(),
                billInfo.pinVirtualTimeT()
        );
    }
}
