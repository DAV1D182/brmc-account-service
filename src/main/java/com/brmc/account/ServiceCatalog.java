package com.brmc.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Definicion general de servicio disponible para activar en cuentas.
 *
 * <p>Esta entidad separa el catalogo operativo de servicios de las instancias ya activadas en una
 * cuenta. El catalogo permite definir nombre, tipo y descripcion una sola vez; luego una cuenta
 * puede activar esa definicion y recibir un {@link BrmService} propio.</p>
 */
@Entity
@Table(name = "service_catalog_t")
class ServiceCatalog {

    @Id
    @Column(length = 30, nullable = false)
    private String id;

    @Column(nullable = false, length = 160)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServiceType serviceType;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServiceCatalogStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_t", nullable = false, updatable = false)
    private Instant createdT;

    @Column(name = "pin_virtual_time_t", nullable = false, updatable = false)
    private LocalDateTime pinVirtualTimeT;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor requerido por JPA.
     */
    protected ServiceCatalog() {
    }

    /**
     * Crea una definicion de servicio de catalogo.
     *
     * @param id identificador visible generado por secuencia.
     * @param name nombre funcional del servicio.
     * @param serviceType tipo de servicio.
     * @param description descripcion opcional.
     * @param status estado inicial del catalogo.
     * @param virtualTime fecha virtual de creacion.
     */
    ServiceCatalog(
            String id,
            String name,
            ServiceType serviceType,
            String description,
            ServiceCatalogStatus status,
            LocalDateTime virtualTime
    ) {
        this.id = id;
        this.name = name;
        this.serviceType = serviceType == null ? ServiceType.GENERIC : serviceType;
        this.description = description;
        this.status = status == null ? ServiceCatalogStatus.ACTIVE : status;
        this.createdAt = virtualTime;
        this.createdT = Instant.now();
        this.pinVirtualTimeT = virtualTime;
        this.updatedAt = virtualTime;
    }

    String id() {
        return id;
    }

    String name() {
        return name;
    }

    ServiceType serviceType() {
        return serviceType;
    }

    String description() {
        return description;
    }

    ServiceCatalogStatus status() {
        return status;
    }

    LocalDateTime createdAt() {
        return createdAt;
    }

    Instant createdT() {
        return createdT;
    }

    LocalDateTime pinVirtualTimeT() {
        return pinVirtualTimeT;
    }

    LocalDateTime updatedAt() {
        return updatedAt;
    }

    void update(
            String name,
            ServiceType serviceType,
            String description,
            ServiceCatalogStatus status,
            LocalDateTime virtualTime
    ) {
        this.name = name;
        this.serviceType = serviceType == null ? ServiceType.GENERIC : serviceType;
        this.description = description;
        this.status = status == null ? ServiceCatalogStatus.ACTIVE : status;
        this.updatedAt = virtualTime;
    }
}
