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
 * Entidad JPA que representa un servicio contratado dentro de una cuenta.
 *
 * <p>Modela el concepto de servicio de BRM para asociar productos facturables a una cuenta. Un
 * servicio inicia en estado ACTIVE, puede suspenderse, reactivarse o terminarse, y bloquea nuevas
 * asignaciones cuando llega a TERMINATED.</p>
 */
@Entity
@Table(name = "services_t")
class BrmService {

    private static final DateTimeFormatter SERVICE_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Id
    @Column(length = 19, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, unique = true, length = 80)
    private String serviceCode;

    @Column(length = 160)
    private String serviceName;

    @Column(length = 30)
    private String catalogServiceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServiceType serviceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServiceStatus status;

    @Column(nullable = false)
    private LocalDateTime activationDate;

    private LocalDateTime terminationDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_t", updatable = false)
    private Instant createdT;

    @Column(name = "pin_virtual_time_t", updatable = false)
    private LocalDateTime pinVirtualTimeT;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor requerido por JPA para materializar servicios persistidos.
     */
    protected BrmService() {
    }

    /**
     * Crea un servicio activo para una cuenta.
     *
     * @param account cuenta propietaria del servicio.
     * @param serviceCode codigo funcional unico.
     * @param serviceType tipo de servicio; GENERIC cuando no se informa.
     * @param activationDate fecha virtual usada como activacion y creacion.
     */
    BrmService(Account account, String serviceCode, ServiceType serviceType, LocalDateTime activationDate) {
        this(newServiceId(), account, serviceCode, null, serviceType, null, activationDate);
    }

    /**
     * Crea un servicio activo para una cuenta desde una definicion general o desde datos directos.
     *
     * @param id identificador visible o tecnico del servicio.
     * @param account cuenta propietaria del servicio.
     * @param serviceCode codigo funcional unico.
     * @param serviceName nombre visible del servicio.
     * @param serviceType tipo de servicio; GENERIC cuando no se informa.
     * @param catalogServiceId definicion de catalogo usada para activar el servicio, si aplica.
     * @param activationDate fecha virtual usada como activacion y creacion.
     */
    BrmService(
            String id,
            Account account,
            String serviceCode,
            String serviceName,
            ServiceType serviceType,
            String catalogServiceId,
            LocalDateTime activationDate
    ) {
        this.id = id;
        this.account = account;
        this.serviceCode = serviceCode;
        this.serviceName = serviceName;
        this.catalogServiceId = catalogServiceId;
        this.serviceType = serviceType == null ? ServiceType.GENERIC : serviceType;
        this.status = ServiceStatus.ACTIVE;
        this.activationDate = activationDate;
        this.createdAt = activationDate;
        this.createdT = Instant.now();
        this.pinVirtualTimeT = activationDate;
        this.updatedAt = activationDate;
    }

    /**
     * Obtiene el identificador interno del servicio.
     *
     * @return id temporal del servicio.
     */
    String id() {
        return id;
    }

    /**
     * Obtiene la cuenta propietaria.
     *
     * @return cuenta asociada.
     */
    Account account() {
        return account;
    }

    /**
     * Obtiene el codigo funcional del servicio.
     *
     * @return codigo unico de servicio.
     */
    String serviceCode() {
        return serviceCode;
    }

    /**
     * Obtiene el nombre visible del servicio.
     *
     * @return nombre del servicio, o {@code null} para registros antiguos.
     */
    String serviceName() {
        return serviceName;
    }

    /**
     * Obtiene la definicion de catalogo usada para activar el servicio.
     *
     * @return id de catalogo o {@code null} si el servicio se creo directamente.
     */
    String catalogServiceId() {
        return catalogServiceId;
    }

    /**
     * Obtiene el tipo de servicio.
     *
     * @return tipo MOBILE, INTERNET, TV o GENERIC.
     */
    ServiceType serviceType() {
        return serviceType;
    }

    /**
     * Obtiene el estado operativo del servicio.
     *
     * @return ACTIVE, SUSPENDED o TERMINATED.
     */
    ServiceStatus status() {
        return status;
    }

    /**
     * Obtiene la fecha de activacion.
     *
     * @return fecha virtual de activacion.
     */
    LocalDateTime activationDate() {
        return activationDate;
    }

    /**
     * Obtiene la fecha de terminacion.
     *
     * @return fecha virtual de terminacion o {@code null} si sigue vigente.
     */
    LocalDateTime terminationDate() {
        return terminationDate;
    }

    /**
     * Obtiene la fecha de creacion.
     *
     * @return fecha virtual de creacion.
     */
    LocalDateTime createdAt() {
        return createdAt;
    }

    Instant createdT() {
        return createdT;
    }

    LocalDateTime pinVirtualTimeT() {
        return pinVirtualTimeT;
    }

    /**
     * Obtiene la ultima fecha de modificacion operativa.
     *
     * @return fecha virtual de ultimo cambio.
     */
    LocalDateTime updatedAt() {
        return updatedAt;
    }

    /**
     * Suspende el servicio sin cancelar sus productos asociados.
     *
     * @param updatedAt fecha virtual del cambio.
     * @throws BusinessRuleException si el servicio ya esta terminado.
     */
    void suspend(LocalDateTime updatedAt) {
        ensureNotTerminated();
        status = ServiceStatus.SUSPENDED;
        this.updatedAt = updatedAt;
    }

    /**
     * Reactiva un servicio suspendido o activo que no este terminado.
     *
     * @param updatedAt fecha virtual del cambio.
     * @throws BusinessRuleException si el servicio ya esta terminado.
     */
    void reactivate(LocalDateTime updatedAt) {
        ensureNotTerminated();
        status = ServiceStatus.ACTIVE;
        this.updatedAt = updatedAt;
    }

    /**
     * Termina el servicio de forma definitiva.
     *
     * @param terminationDate fecha virtual de terminacion.
     * @throws BusinessRuleException si el servicio ya esta terminado.
     */
    void terminate(LocalDateTime terminationDate) {
        ensureNotTerminated();
        status = ServiceStatus.TERMINATED;
        this.terminationDate = terminationDate;
        this.updatedAt = terminationDate;
    }

    /**
     * Actualiza los datos editables del servicio.
     *
     * <p>Conserva la fecha de activacion y registra la fecha virtual de modificacion en
     * {@code updatedAt}. No permite editar servicios terminados.</p>
     *
     * @param serviceCode nuevo codigo funcional.
     * @param serviceName nuevo nombre visible.
     * @param serviceType nuevo tipo de servicio.
     * @param updatedAt fecha virtual del cambio.
     */
    void update(String serviceCode, String serviceName, ServiceType serviceType, LocalDateTime updatedAt) {
        ensureNotTerminated();
        this.serviceCode = serviceCode;
        this.serviceName = serviceName;
        this.serviceType = serviceType == null ? ServiceType.GENERIC : serviceType;
        this.updatedAt = updatedAt;
    }

    /**
     * Valida que el servicio permita nuevas asignaciones de productos.
     *
     * @throws BusinessRuleException si el servicio esta terminado.
     */
    void ensureAssignable() {
        if (status == ServiceStatus.TERMINATED) {
            throw new BusinessRuleException("No se pueden asignar productos a un servicio terminado.");
        }
    }

    private void ensureNotTerminated() {
        if (status == ServiceStatus.TERMINATED) {
            throw new BusinessRuleException("El servicio ya esta terminado.");
        }
    }

    private static String newServiceId() {
        return LocalDateTime.now().format(SERVICE_ID_FORMAT)
                + ThreadLocalRandom.current().nextInt(10, 100);
    }
}
