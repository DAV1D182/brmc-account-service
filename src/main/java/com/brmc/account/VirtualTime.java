package com.brmc.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Entidad JPA de configuracion unica para la fecha virtual del sistema.
 *
 * <p>Simula el uso de {@code pin_virtual_time}: los procesos sensibles al tiempo, especialmente
 * billing y servicios, deben consultar {@link VirtualTimeService} para usar esta fecha logica en
 * lugar de depender directamente del reloj real.</p>
 */
@Entity
@Table(name = "virtual_time_t")
class VirtualTime {

    @Id
    @Column(length = 20, nullable = false)
    private String id;

    @Column(nullable = false)
    private LocalDateTime currentVirtualTime;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_t", updatable = false)
    private Instant createdT;

    @Column(name = "pin_virtual_time_t")
    private LocalDateTime pinVirtualTimeT;

    @Column(length = 80)
    private String updatedBy;

    /**
     * Constructor requerido por JPA para materializar la configuracion persistida.
     */
    protected VirtualTime() {
    }

    /**
     * Crea la configuracion unica de fecha virtual.
     *
     * @param currentVirtualTime fecha/hora logica que usara el sistema.
     * @param updatedBy usuario o actor que realizo el cambio.
     */
    VirtualTime(LocalDateTime currentVirtualTime, String updatedBy) {
        this.id = "BRMC_TIME";
        this.currentVirtualTime = currentVirtualTime;
        this.createdT = Instant.now();
        this.pinVirtualTimeT = currentVirtualTime;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * Obtiene el identificador fijo de la configuracion.
     *
     * @return valor constante BRMC_TIME.
     */
    String id() {
        return id;
    }

    /**
     * Obtiene la fecha virtual vigente.
     *
     * @return fecha/hora logica configurada.
     */
    LocalDateTime currentVirtualTime() {
        return currentVirtualTime;
    }

    /**
     * Obtiene la fecha real de ultima actualizacion.
     *
     * @return fecha/hora en que se guardo el cambio.
     */
    LocalDateTime updatedAt() {
        return updatedAt;
    }

    Instant createdT() {
        return createdT;
    }

    LocalDateTime pinVirtualTimeT() {
        return pinVirtualTimeT;
    }

    /**
     * Obtiene el usuario o actor que actualizo la fecha.
     *
     * @return usuario informado.
     */
    String updatedBy() {
        return updatedBy;
    }

    /**
     * Actualiza la fecha virtual y registra el momento real del cambio.
     *
     * @param currentVirtualTime nueva fecha/hora logica.
     * @param updatedBy usuario o actor responsable.
     */
    void update(LocalDateTime currentVirtualTime, String updatedBy) {
        this.currentVirtualTime = currentVirtualTime;
        if (createdT == null) {
            createdT = Instant.now();
        }
        this.pinVirtualTimeT = currentVirtualTime;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }
}
