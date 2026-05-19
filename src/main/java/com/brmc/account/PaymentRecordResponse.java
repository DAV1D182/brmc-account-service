package com.brmc.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para pagos materializados y su estado de asignacion.
 *
 * @param id identificador del pago.
 * @param accountId cuenta propietaria.
 * @param amount monto contable COP.
 * @param currency moneda contable.
 * @param originalAmount monto recibido.
 * @param originalCurrency moneda recibida.
 * @param exchangeRate tasa usada.
 * @param paymentMethod metodo de pago.
 * @param allocatedAmount monto aplicado a bills o items.
 * @param unallocatedAmount monto pendiente de asignacion.
 * @param allocationStatus estado de asignacion.
 * @param createdAt fecha real de creacion.
 * @param createdT reloj real tecnico.
 * @param pinVirtualTimeT fecha virtual de negocio.
 */
record PaymentRecordResponse(
        String id,
        String accountId,
        BigDecimal amount,
        Currency currency,
        BigDecimal originalAmount,
        Currency originalCurrency,
        BigDecimal exchangeRate,
        PaymentMethod paymentMethod,
        BigDecimal allocatedAmount,
        BigDecimal unallocatedAmount,
        PaymentAllocationStatus allocationStatus,
        Instant createdAt,
        Instant createdT,
        LocalDateTime pinVirtualTimeT
) {

    /**
     * Convierte un pago persistido en respuesta API.
     *
     * @param payment pago materializado.
     * @return DTO de pago con asignacion.
     */
    static PaymentRecordResponse from(PaymentRecord payment) {
        return new PaymentRecordResponse(
                payment.id(),
                payment.account().id(),
                payment.amount(),
                payment.currency(),
                payment.originalAmount(),
                payment.originalCurrency(),
                payment.exchangeRate(),
                payment.paymentMethod(),
                payment.allocatedAmount(),
                payment.unallocatedAmount(),
                payment.allocationStatus(),
                payment.createdAt(),
                payment.createdT(),
                payment.pinVirtualTimeT()
        );
    }
}
