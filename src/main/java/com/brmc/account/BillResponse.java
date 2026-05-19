package com.brmc.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para bills generados.
 *
 * @param id identificador interno.
 * @param billNo numero funcional del bill.
 * @param accountId cuenta facturada.
 * @param billInfoId configuracion usada.
 * @param billingDom DOM usado para el ciclo facturado.
 * @param billingCycle ciclo de facturacion usado.
 * @param billingPeriodLabel etiqueta legible del mes o rango de meses facturado.
 * @param billingRunId corrida origen.
 * @param status estado financiero.
 * @param periodStart inicio del ciclo facturado.
 * @param periodEnd fin del ciclo facturado.
 * @param billDate fecha virtual de emision.
 * @param dueDate fecha de vencimiento sugerida.
 * @param currency moneda del bill.
 * @param totalAmount total facturado.
 * @param paidAmount monto aplicado.
 * @param dueAmount saldo pendiente.
 * @param createdAt fecha real de creacion.
 * @param createdT reloj real tecnico.
 * @param pinVirtualTimeT fecha virtual de negocio.
 * @param items items asociados, cuando la consulta los incluye.
 */
record BillResponse(
        String id,
        String billNo,
        String accountId,
        String billInfoId,
        Integer billingDom,
        String billingCycle,
        String billingPeriodLabel,
        String billingRunId,
        BillStatus status,
        LocalDateTime periodStart,
        LocalDateTime periodEnd,
        LocalDateTime billDate,
        LocalDateTime dueDate,
        Currency currency,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal dueAmount,
        Instant createdAt,
        Instant createdT,
        LocalDateTime pinVirtualTimeT,
        List<BillItemResponse> items
) {

    /**
     * Convierte un bill sin cargar items.
     *
     * @param bill bill persistido.
     * @return DTO sin detalle de items.
     */
    static BillResponse from(Bill bill) {
        return from(bill, List.of());
    }

    /**
     * Convierte un bill con items asociados.
     *
     * @param bill bill persistido.
     * @param items items del bill.
     * @return DTO con detalle opcional.
     */
    static BillResponse from(Bill bill, List<BillItem> items) {
        return new BillResponse(
                bill.id(),
                bill.billNo(),
                bill.account().id(),
                bill.billInfo().id(),
                bill.billInfo().billingDom(),
                bill.billInfo().billingCycle(),
                bill.billingPeriodLabel(),
                bill.billingRun().id(),
                bill.status(),
                bill.periodStart(),
                bill.periodEnd(),
                bill.billDate(),
                bill.dueDate(),
                bill.currency(),
                bill.totalAmount(),
                bill.paidAmount(),
                bill.dueAmount(),
                bill.createdAt(),
                bill.createdT(),
                bill.pinVirtualTimeT(),
                items.stream().map(BillItemResponse::from).toList()
        );
    }
}
