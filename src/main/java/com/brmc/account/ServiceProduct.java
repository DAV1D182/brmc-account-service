package com.brmc.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Entidad JPA que vincula un producto del catalogo con un servicio contratado.
 *
 * <p>Guarda el estado de la asignacion y los marcadores de facturacion usados por billing. Para
 * productos recurrentes inicializa {@code nextBillAt} con la fecha de asignacion, permitiendo que
 * el primer ciclo se facture cuando el proceso de billing alcance esa fecha virtual.</p>
 */
@Entity
@Table(name = "service_products_t")
class ServiceProduct {

    private static final DateTimeFormatter SERVICE_PRODUCT_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Id
    @Column(length = 19, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private BrmService service;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServiceProductStatus status;

    @Column(nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "created_t", updatable = false)
    private Instant createdT;

    @Column(name = "pin_virtual_time_t", updatable = false)
    private LocalDateTime pinVirtualTimeT;

    private LocalDateTime cancelledAt;

    private LocalDateTime lastBilledAt;

    private LocalDateTime nextBillAt;

    /**
     * Constructor requerido por JPA para materializar asignaciones producto-servicio.
     */
    protected ServiceProduct() {
    }

    /**
     * Crea una asignacion activa de producto a servicio.
     *
     * @param service servicio propietario de la asignacion.
     * @param product producto activo del catalogo.
     * @param assignedAt fecha virtual de asignacion.
     */
    ServiceProduct(BrmService service, Product product, LocalDateTime assignedAt) {
        this.id = newServiceProductId();
        this.service = service;
        this.product = product;
        this.status = ServiceProductStatus.ACTIVE;
        this.assignedAt = assignedAt;
        this.createdT = Instant.now();
        this.pinVirtualTimeT = assignedAt;
        this.nextBillAt = product.productType() == ProductType.RECURRING ? assignedAt : null;
    }

    /**
     * Obtiene el identificador de la asignacion.
     *
     * @return id temporal de la relacion servicio-producto.
     */
    String id() {
        return id;
    }

    /**
     * Obtiene el servicio asociado.
     *
     * @return servicio propietario.
     */
    BrmService service() {
        return service;
    }

    /**
     * Obtiene el producto asignado.
     *
     * @return producto del catalogo.
     */
    Product product() {
        return product;
    }

    /**
     * Obtiene el estado de la asignacion.
     *
     * @return ACTIVE o CANCELLED.
     */
    ServiceProductStatus status() {
        return status;
    }

    /**
     * Obtiene la fecha virtual de asignacion.
     *
     * @return fecha en que se contrato el producto.
     */
    LocalDateTime assignedAt() {
        return assignedAt;
    }

    Instant createdT() {
        return createdT;
    }

    LocalDateTime pinVirtualTimeT() {
        return pinVirtualTimeT;
    }

    /**
     * Obtiene la fecha virtual de cancelacion.
     *
     * @return fecha de cancelacion o {@code null} si sigue activo.
     */
    LocalDateTime cancelledAt() {
        return cancelledAt;
    }

    /**
     * Obtiene la ultima fecha facturada.
     *
     * @return fecha virtual del ultimo cargo generado o {@code null}.
     */
    LocalDateTime lastBilledAt() {
        return lastBilledAt;
    }

    /**
     * Obtiene la proxima fecha elegible para billing.
     *
     * @return proxima fecha de cargo recurrente o {@code null} para productos one-time.
     */
    LocalDateTime nextBillAt() {
        return nextBillAt;
    }

    /**
     * Cancela la asignacion de forma idempotente.
     *
     * @param cancelledAt fecha virtual de cancelacion.
     */
    void cancel(LocalDateTime cancelledAt) {
        if (status == ServiceProductStatus.CANCELLED) {
            return;
        }
        status = ServiceProductStatus.CANCELLED;
        this.cancelledAt = cancelledAt;
    }

    /**
     * Marca la asignacion como facturada por billing.
     *
     * <p>Para productos recurrentes mensuales, mueve {@code nextBillAt} un mes hacia adelante. Para
     * productos one-time solo conserva la fecha del cargo generado.</p>
     *
     * @param virtualTime fecha virtual usada por el proceso de billing.
     * @param nextBillAt proxima fecha de ciclo permitida para volver a facturar.
     */
    void markBilled(LocalDateTime virtualTime, LocalDateTime nextBillAt) {
        lastBilledAt = virtualTime;
        if (product.productType() == ProductType.RECURRING && product.billingFrequency() == BillingFrequency.MONTHLY) {
            this.nextBillAt = nextBillAt;
        }
    }

    /**
     * Indica si la asignacion esta activa.
     *
     * @return {@code true} cuando el estado es ACTIVE.
     */
    boolean isActive() {
        return status == ServiceProductStatus.ACTIVE;
    }

    private static String newServiceProductId() {
        return LocalDateTime.now().format(SERVICE_PRODUCT_ID_FORMAT)
                + ThreadLocalRandom.current().nextInt(10, 100);
    }
}
