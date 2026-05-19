package com.brmc.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Entidad JPA que representa un item del inventario operativo.
 *
 * <p>Guarda datos de identificacion, precio unitario, existencias y parametros de reorden. El
 * valor de inventario se calcula cada vez que se crea o actualiza el item como precio unitario por
 * cantidad disponible. La disponibilidad indica si el item sigue habilitado para uso comercial u
 * operativo.</p>
 */
@Entity
@Table(name = "inventory_t")
class InventoryItem {

    private static final DateTimeFormatter INVENTORY_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Id
    @Column(length = 24, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal inventoryValue;

    @Column(nullable = false)
    private Integer reorderLevel;

    @Column(nullable = false)
    private Integer reorderTimeDays;

    @Column(nullable = false)
    private Integer reorderQuantity;

    @Column(nullable = false)
    private Boolean available;

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
     * Constructor requerido por JPA.
     */
    protected InventoryItem() {
    }

    /**
     * Crea un item de inventario.
     *
     * @param name nombre visible del item.
     * @param description descripcion operativa.
     * @param unitPrice precio por unidad.
     * @param stockQuantity cantidad disponible.
     * @param reorderLevel nivel minimo para reorden.
     * @param reorderTimeDays tiempo esperado de reorden en dias.
     * @param reorderQuantity cantidad sugerida de reorden.
     * @param available indica si el item sigue disponible.
     * @param pinVirtualTimeT fecha virtual vigente.
     */
    InventoryItem(
            String name,
            String description,
            BigDecimal unitPrice,
            Integer stockQuantity,
            Integer reorderLevel,
            Integer reorderTimeDays,
            Integer reorderQuantity,
            Boolean available,
            Account account,
            LocalDateTime pinVirtualTimeT
    ) {
        var now = Instant.now();
        this.id = newInventoryId();
        this.account = account;
        this.createdAt = now;
        this.createdT = now;
        this.pinVirtualTimeT = pinVirtualTimeT;
        update(name, description, unitPrice, stockQuantity, reorderLevel, reorderTimeDays, reorderQuantity, available, pinVirtualTimeT);
    }

    /**
     * Obtiene el identificador del inventario.
     *
     * @return id funcional con prefijo INV.
     */
    String id() {
        return id;
    }

    /**
     * Obtiene la cuenta propietaria del item.
     *
     * @return cuenta asociada o {@code null} cuando el item es global/legado.
     */
    Account account() {
        return account;
    }

    /**
     * Asigna el item a una cuenta.
     *
     * @param account cuenta propietaria; {@code null} conserva el item como global.
     */
    void assignAccount(Account account) {
        this.account = account;
    }

    /**
     * Obtiene el nombre del item.
     *
     * @return nombre registrado.
     */
    String name() {
        return name;
    }

    /**
     * Obtiene la descripcion.
     *
     * @return descripcion opcional.
     */
    String description() {
        return description;
    }

    /**
     * Obtiene el precio por unidad.
     *
     * @return precio unitario no negativo.
     */
    BigDecimal unitPrice() {
        return unitPrice;
    }

    /**
     * Obtiene las existencias actuales.
     *
     * @return cantidad disponible.
     */
    Integer stockQuantity() {
        return stockQuantity;
    }

    /**
     * Obtiene el valor total de inventario.
     *
     * @return precio unitario multiplicado por existencias.
     */
    BigDecimal inventoryValue() {
        return inventoryValue;
    }

    /**
     * Obtiene el nivel minimo de reorden.
     *
     * @return cantidad que dispara revision de reorden.
     */
    Integer reorderLevel() {
        return reorderLevel;
    }

    /**
     * Obtiene el tiempo esperado de reorden.
     *
     * @return dias estimados para reponer.
     */
    Integer reorderTimeDays() {
        return reorderTimeDays;
    }

    /**
     * Obtiene la cantidad sugerida de reorden.
     *
     * @return cantidad a solicitar cuando se reordena.
     */
    Integer reorderQuantity() {
        return reorderQuantity;
    }

    /**
     * Indica si el item sigue disponible.
     *
     * @return {@code true} si esta disponible.
     */
    Boolean available() {
        return available;
    }

    /**
     * Obtiene la fecha real de creacion.
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
     * Obtiene la fecha real de actualizacion.
     *
     * @return instante de ultima modificacion.
     */
    Instant updatedAt() {
        return updatedAt;
    }

    LocalDateTime updatedPinVirtualTimeT() {
        return updatedPinVirtualTimeT;
    }

    /**
     * Indica si las existencias estan en o por debajo del nivel de reorden.
     *
     * @return {@code true} cuando debe revisarse reposicion.
     */
    boolean needsReorder() {
        return stockQuantity <= reorderLevel;
    }

    /**
     * Actualiza los datos del item y recalcula el valor total.
     *
     * @param name nombre visible.
     * @param description descripcion operativa.
     * @param unitPrice precio por unidad.
     * @param stockQuantity existencias actuales.
     * @param reorderLevel nivel minimo.
     * @param reorderTimeDays dias de reposicion.
     * @param reorderQuantity cantidad sugerida.
     * @param available disponibilidad actual.
     * @param pinVirtualTimeT fecha virtual vigente.
     */
    void update(
            String name,
            String description,
            BigDecimal unitPrice,
            Integer stockQuantity,
            Integer reorderLevel,
            Integer reorderTimeDays,
            Integer reorderQuantity,
            Boolean available,
            LocalDateTime pinVirtualTimeT
    ) {
        validate(unitPrice, stockQuantity, reorderLevel, reorderTimeDays, reorderQuantity);
        this.name = name == null ? null : name.trim();
        this.description = description;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
        this.inventoryValue = unitPrice.multiply(BigDecimal.valueOf(stockQuantity));
        this.reorderLevel = reorderLevel;
        this.reorderTimeDays = reorderTimeDays;
        this.reorderQuantity = reorderQuantity;
        this.available = available == null ? Boolean.TRUE : available;
        this.updatedAt = Instant.now();
        this.updatedPinVirtualTimeT = pinVirtualTimeT;
    }

    /**
     * Cambia la disponibilidad sin alterar cantidades ni precios.
     *
     * @param available nuevo estado de disponibilidad.
     * @param pinVirtualTimeT fecha virtual vigente.
     */
    void changeAvailability(Boolean available, LocalDateTime pinVirtualTimeT) {
        this.available = available == null ? Boolean.TRUE : available;
        this.updatedAt = Instant.now();
        this.updatedPinVirtualTimeT = pinVirtualTimeT;
    }

    private void validate(
            BigDecimal unitPrice,
            Integer stockQuantity,
            Integer reorderLevel,
            Integer reorderTimeDays,
            Integer reorderQuantity
    ) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("El precio por unidad no puede ser negativo.");
        }
        if (isNegative(stockQuantity) || isNegative(reorderLevel) || isNegative(reorderTimeDays) || isNegative(reorderQuantity)) {
            throw new BusinessRuleException("Las cantidades y tiempos del inventario no pueden ser negativos.");
        }
    }

    private boolean isNegative(Integer value) {
        return value == null || value < 0;
    }

    private static String newInventoryId() {
        return "INV-" + LocalDateTime.now().format(INVENTORY_ID_FORMAT)
                + ThreadLocalRandom.current().nextInt(10, 100);
    }
}
