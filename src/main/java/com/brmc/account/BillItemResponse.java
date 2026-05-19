package com.brmc.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para items de bill.
 *
 * @param id identificador interno.
 * @param itemNo numero funcional del item.
 * @param billId bill contenedor.
 * @param billNo numero funcional del bill.
 * @param billingChargeId cargo origen.
 * @param accountId cuenta facturada.
 * @param serviceId servicio asociado.
 * @param serviceCode codigo del servicio.
 * @param productId producto cobrado.
 * @param productCode codigo del producto.
 * @param productName nombre del producto.
 * @param itemType tipo de cargo.
 * @param status estado contable.
 * @param amount monto facturado.
 * @param currency moneda.
 * @param itemDate fecha virtual.
 * @param description descripcion del cargo.
 * @param createdAt fecha real de creacion.
 * @param createdT reloj real tecnico.
 * @param pinVirtualTimeT fecha virtual de negocio.
 */
record BillItemResponse(
        String id,
        String itemNo,
        String billId,
        String billNo,
        String billingChargeId,
        String accountId,
        String serviceId,
        String serviceCode,
        String productId,
        String productCode,
        String productName,
        ChargeType itemType,
        ItemStatus status,
        BigDecimal amount,
        Currency currency,
        LocalDateTime itemDate,
        String description,
        Instant createdAt,
        Instant createdT,
        LocalDateTime pinVirtualTimeT
) {

    /**
     * Convierte un item persistido en respuesta API.
     *
     * @param item item persistido.
     * @return DTO con trazabilidad de bill, cargo, servicio y producto.
     */
    static BillItemResponse from(BillItem item) {
        return new BillItemResponse(
                item.id(),
                item.itemNo(),
                item.bill().id(),
                item.bill().billNo(),
                item.billingCharge().id(),
                item.account().id(),
                item.service() == null ? null : item.service().id(),
                item.service() == null ? null : item.service().serviceCode(),
                item.product().id(),
                item.product().code(),
                item.product().name(),
                item.itemType(),
                item.status(),
                item.amount(),
                item.currency(),
                item.itemDate(),
                item.description(),
                item.createdAt(),
                item.createdT(),
                item.pinVirtualTimeT()
        );
    }
}
