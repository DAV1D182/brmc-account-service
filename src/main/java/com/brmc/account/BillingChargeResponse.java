package com.brmc.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para cargos individuales de billing.
 *
 * @param id identificador del cargo.
 * @param billingRunId corrida que genero el cargo.
 * @param billId bill asociado al cargo.
 * @param billNo numero funcional del bill.
 * @param billingDom dia del mes usado para el ciclo del bill.
 * @param billingCycle ciclo de facturacion del bill asociado.
 * @param billingPeriodLabel etiqueta legible del mes o rango de meses facturado.
 * @param billPeriodStart inicio del periodo facturado.
 * @param billPeriodEnd fin del periodo facturado.
 * @param accountId cuenta facturada.
 * @param serviceId servicio facturado, si aplica.
 * @param serviceCode codigo del servicio.
 * @param serviceProductId asignacion servicio-producto facturada.
 * @param productId producto cobrado.
 * @param productCode codigo funcional del producto.
 * @param productName nombre comercial del producto.
 * @param chargeType tipo de cargo.
 * @param amount monto cobrado.
 * @param currency moneda del cargo.
 * @param chargeDate fecha virtual del cargo.
 * @param description descripcion generada por billing.
 * @param transactionId transaccion financiera asociada.
 * @param createdAt fecha real de creacion.
 */
record BillingChargeResponse(
        String id,
        String billingRunId,
        String billId,
        String billNo,
        Integer billingDom,
        String billingCycle,
        String billingPeriodLabel,
        LocalDateTime billPeriodStart,
        LocalDateTime billPeriodEnd,
        String accountId,
        String serviceId,
        String serviceCode,
        String serviceProductId,
        String productId,
        String productCode,
        String productName,
        ChargeType chargeType,
        BigDecimal amount,
        Currency currency,
        LocalDateTime chargeDate,
        Instant createdT,
        LocalDateTime pinVirtualTimeT,
        String description,
        String transactionId,
        LocalDateTime createdAt
) {

    /**
     * Convierte un cargo de billing en respuesta API.
     *
     * @param charge cargo persistido.
     * @return DTO con datos de cuenta, servicio, producto y transaccion.
     */
    static BillingChargeResponse from(BillingCharge charge) {
        return new BillingChargeResponse(
                charge.id(),
                charge.billingRun().id(),
                charge.bill() == null ? null : charge.bill().id(),
                charge.bill() == null ? null : charge.bill().billNo(),
                charge.bill() == null ? null : charge.bill().billInfo().billingDom(),
                charge.bill() == null ? null : charge.bill().billInfo().billingCycle(),
                charge.bill() == null ? null : charge.bill().billingPeriodLabel(),
                charge.bill() == null ? null : charge.bill().periodStart(),
                charge.bill() == null ? null : charge.bill().periodEnd(),
                charge.account().id(),
                charge.service() == null ? null : charge.service().id(),
                charge.service() == null ? null : charge.service().serviceCode(),
                charge.serviceProduct() == null ? null : charge.serviceProduct().id(),
                charge.product().id(),
                charge.product().code(),
                charge.product().name(),
                charge.chargeType(),
                charge.amount(),
                charge.currency(),
                charge.chargeDate(),
                charge.createdT(),
                charge.pinVirtualTimeT(),
                charge.description(),
                charge.transactionId(),
                charge.createdAt()
        );
    }
}
