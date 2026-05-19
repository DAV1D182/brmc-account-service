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
 * Entidad JPA del catalogo comercial de productos.
 *
 * <p>Define ofertas facturables de tipo unico o recurrente. La entidad valida reglas basicas del
 * catalogo: precio no negativo, frecuencia NONE para productos ONE_TIME y frecuencia distinta de
 * NONE para productos RECURRING. El codigo se normaliza en mayusculas para mantener unicidad
 * funcional en {@code products_t}.</p>
 */
@Entity
@Table(name = "products_t")
class Product {

    private static final DateTimeFormatter PRODUCT_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Id
    @Column(length = 19, nullable = false)
    private String id;

    @Column(name = "display_id", unique = true, length = 40)
    private String displayId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(nullable = false, length = 80)
    private String code;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductType productType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillingFrequency billingFrequency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_t", updatable = false)
    private Instant createdT;

    @Column(name = "pin_virtual_time_t", updatable = false)
    private LocalDateTime pinVirtualTimeT;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_pin_virtual_time_t")
    private LocalDateTime updatedPinVirtualTimeT;

    /**
     * Constructor requerido por JPA para materializar productos persistidos.
     */
    protected Product() {
    }

    /**
     * Crea un producto comercial y aplica las mismas validaciones usadas por actualizacion.
     *
     * @param code codigo funcional unico.
     * @param name nombre comercial.
     * @param description descripcion opcional del producto.
     * @param productType tipo de cobro del producto.
     * @param price precio base.
     * @param currency moneda del precio; COP por defecto.
     * @param billingFrequency frecuencia de billing.
     * @param status estado inicial; ACTIVE por defecto.
     * @throws BusinessRuleException si el precio o la frecuencia no cumplen las reglas del catalogo.
     */
    Product(
            String code,
            String name,
            String description,
            ProductType productType,
            BigDecimal price,
            Currency currency,
            BillingFrequency billingFrequency,
            ProductStatus status,
            String displayId,
            Account account,
            LocalDateTime pinVirtualTimeT
    ) {
        var now = Instant.now();
        this.id = newProductId();
        this.displayId = displayId;
        this.account = account;
        this.createdAt = now;
        this.createdT = now;
        this.pinVirtualTimeT = pinVirtualTimeT;
        this.updatedPinVirtualTimeT = pinVirtualTimeT;
        update(code, name, description, productType, price, currency, billingFrequency, status);
    }

    /**
     * Obtiene el identificador interno del producto.
     *
     * @return id temporal del producto.
     */
    String id() {
        return id;
    }

    /**
     * Obtiene el identificador visible corto del producto.
     *
     * @return display id como PRD-101; si el dato historico es nulo, retorna el id tecnico.
     */
    String displayId() {
        return displayId == null || displayId.isBlank() ? id : displayId;
    }

    /**
     * Obtiene la cuenta propietaria del producto.
     *
     * @return cuenta asociada o {@code null} cuando el producto pertenece al catalogo global.
     */
    Account account() {
        return account;
    }

    /**
     * Asigna el producto a una cuenta especifica.
     *
     * @param account cuenta propietaria; {@code null} mantiene el producto como global.
     */
    void assignAccount(Account account) {
        this.account = account;
    }

    /**
     * Obtiene el codigo funcional normalizado.
     *
     * @return codigo unico en mayusculas.
     */
    String code() {
        return code;
    }

    /**
     * Obtiene el nombre comercial.
     *
     * @return nombre del producto.
     */
    String name() {
        return name;
    }

    /**
     * Obtiene la descripcion del producto.
     *
     * @return descripcion opcional.
     */
    String description() {
        return description;
    }

    /**
     * Obtiene el tipo de producto.
     *
     * @return ONE_TIME o RECURRING.
     */
    ProductType productType() {
        return productType;
    }

    /**
     * Obtiene el precio configurado.
     *
     * @return precio no negativo.
     */
    BigDecimal price() {
        return price;
    }

    /**
     * Obtiene la moneda del precio.
     *
     * @return moneda configurada o COP cuando el valor historico es nulo.
     */
    Currency currency() {
        return currency == null ? Currency.COP : currency;
    }

    /**
     * Obtiene la frecuencia usada por billing.
     *
     * @return NONE para cobros unicos o MONTHLY para recurrentes mensuales.
     */
    BillingFrequency billingFrequency() {
        return billingFrequency;
    }

    /**
     * Obtiene el estado comercial del producto.
     *
     * @return ACTIVE o INACTIVE.
     */
    ProductStatus status() {
        return status;
    }

    /**
     * Obtiene la fecha de creacion.
     *
     * @return instante de creacion.
     */
    Instant createdAt() {
        return createdAt;
    }

    Instant createdT() {
        return createdT == null ? createdAt : createdT;
    }

    LocalDateTime pinVirtualTimeT() {
        return pinVirtualTimeT;
    }

    /**
     * Obtiene la fecha de ultima actualizacion.
     *
     * @return instante actualizado al modificar estado o datos.
     */
    Instant updatedAt() {
        return updatedAt;
    }

    LocalDateTime updatedPinVirtualTimeT() {
        return updatedPinVirtualTimeT;
    }

    /**
     * Actualiza los datos comerciales del producto.
     *
     * @param code codigo funcional a normalizar.
     * @param name nombre comercial.
     * @param description descripcion opcional.
     * @param productType tipo de cobro.
     * @param price precio no negativo.
     * @param currency moneda del precio; COP si es nula.
     * @param billingFrequency frecuencia solicitada; se calcula por defecto si es nula.
     * @param status estado comercial; ACTIVE si es nulo.
     * @throws BusinessRuleException si el precio o la frecuencia son incompatibles con el tipo.
     */
    void update(
            String code,
            String name,
            String description,
            ProductType productType,
            BigDecimal price,
            Currency currency,
            BillingFrequency billingFrequency,
            ProductStatus status,
            LocalDateTime pinVirtualTimeT
    ) {
        var normalizedFrequency = billingFrequency == null
                ? (productType == ProductType.RECURRING ? BillingFrequency.MONTHLY : BillingFrequency.NONE)
                : billingFrequency;
        validate(productType, price, normalizedFrequency);
        this.code = code == null ? null : code.trim().toUpperCase();
        this.name = name == null ? null : name.trim();
        this.description = description;
        this.productType = productType;
        this.price = price;
        this.currency = currency == null ? Currency.COP : currency;
        this.billingFrequency = normalizedFrequency;
        this.status = status == null ? ProductStatus.ACTIVE : status;
        this.updatedAt = Instant.now();
        this.updatedPinVirtualTimeT = pinVirtualTimeT;
    }

    void update(
            String code,
            String name,
            String description,
            ProductType productType,
            BigDecimal price,
            Currency currency,
            BillingFrequency billingFrequency,
            ProductStatus status
    ) {
        update(code, name, description, productType, price, currency, billingFrequency, status, updatedPinVirtualTimeT);
    }

    /**
     * Activa el producto para que pueda ser asignado a servicios.
     */
    void activate(LocalDateTime pinVirtualTimeT) {
        this.status = ProductStatus.ACTIVE;
        this.updatedAt = Instant.now();
        this.updatedPinVirtualTimeT = pinVirtualTimeT;
    }

    /**
     * Inactiva el producto e impide nuevas asignaciones.
     */
    void deactivate(LocalDateTime pinVirtualTimeT) {
        this.status = ProductStatus.INACTIVE;
        this.updatedAt = Instant.now();
        this.updatedPinVirtualTimeT = pinVirtualTimeT;
    }

    private void validate(ProductType productType, BigDecimal price, BillingFrequency billingFrequency) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("El precio del producto no puede ser negativo.");
        }

        if (productType == ProductType.ONE_TIME && billingFrequency != null && billingFrequency != BillingFrequency.NONE) {
            throw new BusinessRuleException("Los productos ONE_TIME deben usar frecuencia NONE.");
        }

        if (productType == ProductType.RECURRING && billingFrequency == BillingFrequency.NONE) {
            throw new BusinessRuleException("Los productos RECURRING deben tener una frecuencia de billing.");
        }
    }

    private static String newProductId() {
        return LocalDateTime.now().format(PRODUCT_ID_FORMAT)
                + ThreadLocalRandom.current().nextInt(10, 100);
    }
}
