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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Entidad JPA que representa un cargo generado por billing.
 *
 * <p>Vincula una corrida de billing con la cuenta, servicio, producto asignado y transaccion
 * financiera creada para impactar el saldo. El monto y la moneda se toman del producto al momento
 * de generar el cargo.</p>
 */
@Entity
@Table(name = "billing_charges_t")
class BillingCharge {

    private static final DateTimeFormatter BILLING_CHARGE_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Id
    @Column(length = 19, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "billing_run_id", nullable = false)
    private BillingRun billingRun;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bill_id")
    private Bill bill;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id")
    private BrmService service;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_product_id")
    private ServiceProduct serviceProduct;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChargeType chargeType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(nullable = false)
    private LocalDateTime chargeDate;

    @Column(name = "created_t", updatable = false)
    private Instant createdT;

    @Column(name = "pin_virtual_time_t", updatable = false)
    private LocalDateTime pinVirtualTimeT;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(length = 19)
    private String transactionId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Constructor requerido por JPA para materializar cargos de billing.
     */
    protected BillingCharge() {
    }

    /**
     * Crea un cargo de billing para un producto asignado.
     *
     * @param billingRun corrida que genero el cargo.
     * @param bill bill que agrupa el cargo como item.
     * @param account cuenta impactada por la facturacion.
     * @param service servicio asociado al producto.
     * @param serviceProduct asignacion facturada.
     * @param product producto del catalogo que define precio y moneda.
     * @param chargeType tipo de cargo generado.
     * @param chargeDate fecha virtual de facturacion.
     * @param transactionId transaccion financiera creada en la cuenta.
     */
    BillingCharge(
            BillingRun billingRun,
            Bill bill,
            Account account,
            BrmService service,
            ServiceProduct serviceProduct,
            Product product,
            ChargeType chargeType,
            LocalDateTime chargeDate,
            String transactionId
    ) {
        this.id = newBillingChargeId();
        this.billingRun = billingRun;
        this.bill = bill;
        this.account = account;
        this.service = service;
        this.serviceProduct = serviceProduct;
        this.product = product;
        this.chargeType = chargeType;
        this.amount = product.price();
        this.currency = product.currency();
        this.chargeDate = chargeDate;
        this.createdT = Instant.now();
        this.pinVirtualTimeT = chargeDate;
        this.description = descriptionFor(product, chargeType);
        this.transactionId = transactionId;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Obtiene el identificador del cargo.
     *
     * @return id temporal del cargo.
     */
    String id() {
        return id;
    }

    /**
     * Obtiene la corrida que genero el cargo.
     *
     * @return billing run asociado.
     */
    BillingRun billingRun() {
        return billingRun;
    }

    /**
     * Obtiene el bill que agrupa el cargo.
     *
     * @return bill asociado o {@code null} en cargos historicos previos a items.
     */
    Bill bill() {
        return bill;
    }

    /**
     * Obtiene la cuenta facturada.
     *
     * @return cuenta impactada.
     */
    Account account() {
        return account;
    }

    /**
     * Obtiene el servicio facturado.
     *
     * @return servicio asociado o {@code null} si el cargo no lo informa.
     */
    BrmService service() {
        return service;
    }

    /**
     * Obtiene la asignacion servicio-producto facturada.
     *
     * @return asignacion origen o {@code null} si no aplica.
     */
    ServiceProduct serviceProduct() {
        return serviceProduct;
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
     * Obtiene el tipo de cargo.
     *
     * @return ONE_TIME o RECURRING.
     */
    ChargeType chargeType() {
        return chargeType;
    }

    /**
     * Obtiene el monto cobrado.
     *
     * @return precio del producto al crear el cargo.
     */
    BigDecimal amount() {
        return amount;
    }

    /**
     * Obtiene la moneda del cargo.
     *
     * @return moneda del producto.
     */
    Currency currency() {
        return currency;
    }

    /**
     * Obtiene la fecha virtual del cargo.
     *
     * @return fecha usada por billing.
     */
    LocalDateTime chargeDate() {
        return chargeDate;
    }

    Instant createdT() {
        return createdT;
    }

    LocalDateTime pinVirtualTimeT() {
        return pinVirtualTimeT == null ? chargeDate : pinVirtualTimeT;
    }

    /**
     * Obtiene la descripcion generada por billing.
     *
     * @return texto de cargo visible en consultas.
     */
    String description() {
        return description;
    }

    /**
     * Obtiene la transaccion financiera asociada.
     *
     * @return id de {@code account_transactions_t}.
     */
    String transactionId() {
        return transactionId;
    }

    /**
     * Obtiene la fecha real de creacion del registro.
     *
     * @return fecha/hora de persistencia.
     */
    LocalDateTime createdAt() {
        return createdAt;
    }

    private String descriptionFor(Product product, ChargeType chargeType) {
        if (chargeType == ChargeType.RECURRING) {
            return "Recurring billing charge - " + product.code();
        }
        return "One-time billing charge - " + product.code();
    }

    private static String newBillingChargeId() {
        return LocalDateTime.now().format(BILLING_CHARGE_ID_FORMAT)
                + ThreadLocalRandom.current().nextInt(10, 100);
    }
}
