package com.brmc.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Entidad JPA que representa un item dentro de un bill.
 *
 * <p>Modela de forma simplificada el objeto {@code /item} de BRM. Cada item nace desde un
 * {@link BillingCharge}, queda asociado al bill de la corrida y mantiene la trazabilidad hacia
 * cuenta, servicio y producto.</p>
 */
@Entity
@Table(name = "items_t")
class BillItem {

    private static final DateTimeFormatter ITEM_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Id
    @Column(length = 19, nullable = false)
    private String id;

    @Column(nullable = false, unique = true, length = 80)
    private String itemNo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "billing_charge_id", nullable = false, unique = true)
    private BillingCharge billingCharge;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id")
    private BrmService service;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChargeType itemType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(nullable = false)
    private LocalDateTime itemDate;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_t", updatable = false)
    private Instant createdT;

    @Column(name = "pin_virtual_time_t", updatable = false)
    private LocalDateTime pinVirtualTimeT;

    /**
     * Constructor requerido por JPA.
     */
    protected BillItem() {
    }

    /**
     * Crea un item desde un cargo de billing.
     *
     * @param bill bill que agrupa el item.
     * @param billingCharge cargo origen.
     */
    BillItem(Bill bill, BillingCharge billingCharge) {
        this.id = newItemId();
        this.itemNo = "ITEM-" + id;
        this.bill = bill;
        this.billingCharge = billingCharge;
        this.account = billingCharge.account();
        this.service = billingCharge.service();
        this.product = billingCharge.product();
        this.itemType = billingCharge.chargeType();
        this.status = ItemStatus.OPEN;
        this.amount = billingCharge.amount();
        this.currency = billingCharge.currency();
        this.itemDate = billingCharge.chargeDate();
        this.description = billingCharge.description();
        this.createdAt = Instant.now();
        this.createdT = createdAt;
        this.pinVirtualTimeT = billingCharge.pinVirtualTimeT();
    }

    /**
     * Obtiene el identificador interno.
     *
     * @return id persistido.
     */
    String id() {
        return id;
    }

    /**
     * Obtiene el numero funcional del item.
     *
     * @return item no visible para consultas.
     */
    String itemNo() {
        return itemNo;
    }

    /**
     * Obtiene el bill asociado.
     *
     * @return bill contenedor.
     */
    Bill bill() {
        return bill;
    }

    /**
     * Obtiene el cargo origen.
     *
     * @return cargo de billing.
     */
    BillingCharge billingCharge() {
        return billingCharge;
    }

    /**
     * Obtiene la cuenta propietaria.
     *
     * @return cuenta facturada.
     */
    Account account() {
        return account;
    }

    /**
     * Obtiene el servicio asociado.
     *
     * @return servicio o {@code null}.
     */
    BrmService service() {
        return service;
    }

    /**
     * Obtiene el producto cobrado.
     *
     * @return producto del catalogo.
     */
    Product product() {
        return product;
    }

    /**
     * Obtiene el tipo de item.
     *
     * @return ONE_TIME o RECURRING.
     */
    ChargeType itemType() {
        return itemType;
    }

    /**
     * Obtiene el estado contable del item.
     *
     * @return estado actual.
     */
    ItemStatus status() {
        return status;
    }

    /**
     * Obtiene el monto del item.
     *
     * @return monto facturado.
     */
    BigDecimal amount() {
        return amount;
    }

    /**
     * Obtiene la moneda del item.
     *
     * @return moneda contable.
     */
    Currency currency() {
        return currency;
    }

    /**
     * Obtiene la fecha virtual del item.
     *
     * @return fecha del cargo.
     */
    LocalDateTime itemDate() {
        return itemDate;
    }

    /**
     * Obtiene la descripcion del item.
     *
     * @return texto heredado del cargo.
     */
    String description() {
        return description;
    }

    /**
     * Obtiene la fecha real de creacion.
     *
     * @return instante real de persistencia.
     */
    Instant createdAt() {
        return createdAt;
    }

    /**
     * Obtiene el reloj real tecnico de creacion.
     *
     * @return instante real tecnico.
     */
    Instant createdT() {
        return createdT == null ? createdAt : createdT;
    }

    /**
     * Obtiene el pin virtual time usado al crear el item.
     *
     * @return fecha virtual de negocio.
     */
    LocalDateTime pinVirtualTimeT() {
        return pinVirtualTimeT == null ? itemDate : pinVirtualTimeT;
    }

    private static String newItemId() {
        return LocalDateTime.now().format(ITEM_ID_FORMAT)
                + ThreadLocalRandom.current().nextInt(10, 100);
    }
}
