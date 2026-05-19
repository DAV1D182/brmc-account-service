package com.brmc.account;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicacion para administrar servicios y productos asignados.
 *
 * <p>Representa la capa de negocio del modulo "service" inspirado en BRM. Usa fecha virtual para
 * altas, bajas y cambios de estado, valida que la cuenta no este cerrada y registra eventos de
 * auditoria en cada operacion funcional.</p>
 */
@Service
@Transactional
class ServiceManagementService {

    private static final DateTimeFormatter SERVICE_CODE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;
    private final BrmServiceRepository serviceRepository;
    private final ServiceCatalogRepository serviceCatalogRepository;
    private final ServiceProductRepository serviceProductRepository;
    private final VirtualTimeService virtualTimeService;
    private final SystemEventRepository eventRepository;
    private final IdSequenceService idSequenceService;
    private final UserContextService userContextService;

    /**
     * Crea el servicio de administracion de servicios.
     *
     * @param accountRepository repositorio de cuentas.
     * @param productRepository repositorio de productos.
     * @param serviceRepository repositorio de servicios.
     * @param serviceProductRepository repositorio de asignaciones producto-servicio.
     * @param virtualTimeService proveedor de fecha virtual.
     * @param eventRepository repositorio de auditoria.
     */
    ServiceManagementService(
            AccountRepository accountRepository,
            ProductRepository productRepository,
            BrmServiceRepository serviceRepository,
            ServiceCatalogRepository serviceCatalogRepository,
            ServiceProductRepository serviceProductRepository,
            VirtualTimeService virtualTimeService,
            SystemEventRepository eventRepository,
            IdSequenceService idSequenceService,
            UserContextService userContextService
    ) {
        this.accountRepository = accountRepository;
        this.productRepository = productRepository;
        this.serviceRepository = serviceRepository;
        this.serviceCatalogRepository = serviceCatalogRepository;
        this.serviceProductRepository = serviceProductRepository;
        this.virtualTimeService = virtualTimeService;
        this.eventRepository = eventRepository;
        this.idSequenceService = idSequenceService;
        this.userContextService = userContextService;
    }

    /**
     * Crea una definicion general de servicio que luego puede activarse en una cuenta.
     *
     * @param name nombre visible del servicio.
     * @param serviceType tipo funcional.
     * @param description descripcion opcional.
     * @param status estado de disponibilidad.
     * @return definicion persistida.
     * @throws BusinessRuleException si el nombre falta.
     */
    ServiceCatalog createCatalogService(
            String name,
            ServiceType serviceType,
            String description,
            ServiceCatalogStatus status
    ) {
        var normalizedName = normalizeName(name);
        var now = virtualTimeService.getCurrentVirtualTime();
        var catalog = serviceCatalogRepository.save(new ServiceCatalog(
                idSequenceService.nextId("SERVICE"),
                normalizedName,
                serviceType,
                normalizeNullable(description),
                status,
                now
        ));
        logEvent(EventType.SERVICE_CATALOG_CREATED, "SERVICE_CATALOG", catalog.id(), null,
                "Servicio general creado: " + catalog.name() + ".");
        return catalog;
    }

    /**
     * Actualiza una definicion general de servicio.
     *
     * @param catalogServiceId definicion a modificar.
     * @param name nombre visible.
     * @param serviceType tipo funcional.
     * @param description descripcion opcional.
     * @param status estado de disponibilidad.
     * @return definicion actualizada.
     * @throws BrmServiceNotFoundException si la definicion no existe.
     * @throws BusinessRuleException si el nombre falta.
     */
    ServiceCatalog updateCatalogService(
            String catalogServiceId,
            String name,
            ServiceType serviceType,
            String description,
            ServiceCatalogStatus status
    ) {
        var catalog = getCatalogService(catalogServiceId);
        catalog.update(
                normalizeName(name),
                serviceType,
                normalizeNullable(description),
                status,
                virtualTimeService.getCurrentVirtualTime()
        );
        var saved = serviceCatalogRepository.save(catalog);
        logEvent(EventType.SERVICE_CATALOG_UPDATED, "SERVICE_CATALOG", saved.id(), null,
                "Servicio general actualizado: " + saved.name() + ".");
        return saved;
    }

    /**
     * Activa una definicion general de servicio en una cuenta.
     *
     * @param accountId cuenta propietaria.
     * @param catalogServiceId definicion general activa.
     * @return servicio de cuenta creado.
     * @throws AccountNotFoundException si la cuenta no existe.
     * @throws AccountClosedException si la cuenta esta cerrada.
     * @throws BrmServiceNotFoundException si la definicion no existe.
     * @throws BusinessRuleException si la definicion esta inactiva.
     */
    BrmService activateCatalogService(String accountId, String catalogServiceId) {
        var account = getAccount(accountId);
        ensureAccountActive(account);
        var catalog = getCatalogService(catalogServiceId);
        if (catalog.status() != ServiceCatalogStatus.ACTIVE) {
            throw new BusinessRuleException("Solo se pueden activar servicios generales ACTIVE.");
        }
        var now = virtualTimeService.getCurrentVirtualTime();
        var serviceId = idSequenceService.nextId("SERVICE");
        var normalizedCode = generatedServiceCode(accountId, catalog.serviceType(), serviceId);
        var service = serviceRepository.save(new BrmService(
                serviceId,
                account,
                normalizedCode,
                catalog.name(),
                catalog.serviceType(),
                catalog.id(),
                now
        ));
        logEvent(EventType.SERVICE_ACTIVATED, "SERVICE", service.id(), account.id(),
                "Servicio general " + catalog.name() + " activado en cuenta.");
        logEvent(EventType.SERVICE_CREATED, "SERVICE", service.id(), account.id(),
                "Servicio creado: " + service.serviceCode() + ".");
        return service;
    }

    /**
     * Crea un servicio activo para una cuenta activa.
     *
     * @param accountId cuenta propietaria.
     * @param serviceCode codigo opcional; si falta se genera con tipo, cuenta y fecha virtual.
     * @param serviceType tipo de servicio; GENERIC si falta.
     * @return servicio persistido.
     * @throws AccountNotFoundException si la cuenta no existe.
     * @throws AccountClosedException si la cuenta esta cerrada.
     * @throws BusinessRuleException si el codigo ya existe.
     */
    BrmService createService(String accountId, String serviceCode, ServiceType serviceType) {
        return createService(accountId, null, serviceCode, serviceType);
    }

    /**
     * Crea directamente un servicio activo para una cuenta activa.
     *
     * @param accountId cuenta propietaria.
     * @param serviceName nombre visible opcional.
     * @param serviceCode codigo opcional; si falta se genera con tipo, cuenta y fecha virtual.
     * @param serviceType tipo de servicio; GENERIC si falta.
     * @return servicio persistido.
     * @throws AccountNotFoundException si la cuenta no existe.
     * @throws AccountClosedException si la cuenta esta cerrada.
     * @throws BusinessRuleException si el codigo ya existe.
     */
    BrmService createService(String accountId, String serviceName, String serviceCode, ServiceType serviceType) {
        var account = getAccount(accountId);
        ensureAccountActive(account);
        var normalizedCode = normalizeServiceCode(serviceCode, accountId, serviceType);
        if (serviceRepository.existsByServiceCode(normalizedCode)) {
            throw new BusinessRuleException("Ya existe un servicio con codigo " + normalizedCode + ".");
        }

        var now = virtualTimeService.getCurrentVirtualTime();
        var serviceId = idSequenceService.nextId("SERVICE");
        var service = serviceRepository.save(new BrmService(
                serviceId,
                account,
                serviceCode == null || serviceCode.isBlank() ? generatedServiceCode(accountId, serviceType, serviceId) : normalizedCode,
                serviceName == null || serviceName.isBlank() ? defaultServiceName(serviceType) : serviceName.trim(),
                serviceType,
                null,
                now
        ));
        logEvent(EventType.SERVICE_CREATED, "SERVICE", service.id(), account.id(),
                "Servicio creado: " + service.serviceCode() + ".");
        return service;
    }

    /**
     * Actualiza los datos editables de un servicio.
     *
     * @param serviceId servicio a modificar.
     * @param serviceCode nuevo codigo funcional.
     * @param serviceType nuevo tipo de servicio.
     * @return servicio actualizado.
     * @throws BrmServiceNotFoundException si el servicio no existe.
     * @throws AccountClosedException si la cuenta propietaria esta cerrada.
     * @throws BusinessRuleException si el servicio esta terminado o el codigo ya esta en uso.
     */
    BrmService updateService(String serviceId, String serviceCode, ServiceType serviceType) {
        return updateService(serviceId, null, serviceCode, serviceType);
    }

    /**
     * Actualiza los datos editables de un servicio.
     *
     * @param serviceId servicio a modificar.
     * @param serviceName nuevo nombre visible.
     * @param serviceCode nuevo codigo funcional.
     * @param serviceType nuevo tipo de servicio.
     * @return servicio actualizado.
     * @throws BrmServiceNotFoundException si el servicio no existe.
     * @throws AccountClosedException si la cuenta propietaria esta cerrada.
     * @throws BusinessRuleException si el servicio esta terminado o el codigo ya esta en uso.
     */
    BrmService updateService(String serviceId, String serviceName, String serviceCode, ServiceType serviceType) {
        var service = getService(serviceId);
        ensureAccountActive(service.account());
        var normalizedCode = normalizeServiceCode(serviceCode, service.account().id(), serviceType);
        serviceRepository.findByServiceCode(normalizedCode)
                .filter(existing -> !existing.id().equals(serviceId))
                .ifPresent(existing -> {
                    throw new BusinessRuleException("Ya existe un servicio con codigo " + normalizedCode + ".");
                });
        service.update(
                normalizedCode,
                serviceName == null || serviceName.isBlank() ? defaultServiceName(serviceType) : serviceName.trim(),
                serviceType,
                virtualTimeService.getCurrentVirtualTime()
        );
        var saved = serviceRepository.save(service);
        logEvent(EventType.SERVICE_UPDATED, "SERVICE", saved.id(), saved.account().id(),
                "Servicio actualizado: " + saved.serviceCode() + ".");
        return saved;
    }

    /**
     * Suspende un servicio de una cuenta activa.
     *
     * @param serviceId servicio a suspender.
     * @return servicio actualizado.
     * @throws BrmServiceNotFoundException si el servicio no existe.
     * @throws AccountClosedException si la cuenta esta cerrada.
     * @throws BusinessRuleException si el servicio ya esta terminado.
     */
    BrmService suspendService(String serviceId) {
        var service = getService(serviceId);
        ensureAccountActive(service.account());
        service.suspend(virtualTimeService.getCurrentVirtualTime());
        var saved = serviceRepository.save(service);
        logEvent(EventType.SERVICE_SUSPENDED, "SERVICE", saved.id(), saved.account().id(),
                "Servicio suspendido: " + saved.serviceCode() + ".");
        return saved;
    }

    /**
     * Reactiva un servicio no terminado.
     *
     * @param serviceId servicio a reactivar.
     * @return servicio actualizado.
     * @throws BrmServiceNotFoundException si el servicio no existe.
     * @throws AccountClosedException si la cuenta esta cerrada.
     * @throws BusinessRuleException si el servicio ya esta terminado.
     */
    BrmService reactivateService(String serviceId) {
        var service = getService(serviceId);
        ensureAccountActive(service.account());
        service.reactivate(virtualTimeService.getCurrentVirtualTime());
        var saved = serviceRepository.save(service);
        logEvent(EventType.SERVICE_REACTIVATED, "SERVICE", saved.id(), saved.account().id(),
                "Servicio reactivado: " + saved.serviceCode() + ".");
        return saved;
    }

    /**
     * Termina un servicio y cancela sus productos activos.
     *
     * @param serviceId servicio a terminar.
     * @return servicio terminado.
     * @throws BrmServiceNotFoundException si el servicio no existe.
     * @throws AccountClosedException si la cuenta esta cerrada.
     * @throws BusinessRuleException si el servicio ya esta terminado.
     */
    BrmService terminateService(String serviceId) {
        var service = getService(serviceId);
        ensureAccountActive(service.account());
        var now = virtualTimeService.getCurrentVirtualTime();
        service.terminate(now);
        serviceProductRepository.findByServiceIdAndStatusOrderByAssignedAtAsc(serviceId, ServiceProductStatus.ACTIVE)
                .forEach(product -> product.cancel(now));
        var saved = serviceRepository.save(service);
        logEvent(EventType.SERVICE_TERMINATED, "SERVICE", saved.id(), saved.account().id(),
                "Servicio terminado: " + saved.serviceCode() + ".");
        return saved;
    }

    /**
     * Asigna un producto activo a un servicio activo.
     *
     * <p>Bloquea asignaciones sobre cuentas cerradas, servicios suspendidos o terminados,
     * productos inactivos y duplicados activos del mismo producto en el mismo servicio.</p>
     *
     * @param serviceId servicio receptor.
     * @param productId producto a asignar.
     * @return asignacion creada.
     * @throws BrmServiceNotFoundException si el servicio no existe.
     * @throws ProductNotFoundException si el producto no existe.
     * @throws AccountClosedException si la cuenta esta cerrada.
     * @throws BusinessRuleException si la asignacion no cumple las reglas operativas.
     */
    ServiceProduct assignProduct(String serviceId, String productId) {
        var service = getService(serviceId);
        ensureAccountActive(service.account());
        service.ensureAssignable();
        if (service.status() == ServiceStatus.SUSPENDED) {
            throw new BusinessRuleException("No se pueden asignar productos a un servicio suspendido.");
        }

        var product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("No existe un producto con id " + productId + "."));
        if (product.account() != null && !product.account().id().equals(service.account().id())) {
            throw new BusinessRuleException("El producto pertenece a otra cuenta y no puede asignarse a este servicio.");
        }
        if (product.status() != ProductStatus.ACTIVE) {
            throw new BusinessRuleException("Solo se pueden asignar productos ACTIVE.");
        }
        if (serviceProductRepository.existsByServiceIdAndProductIdAndStatus(serviceId, productId, ServiceProductStatus.ACTIVE)) {
            throw new BusinessRuleException("El producto ya esta asignado activamente a este servicio.");
        }

        var serviceProduct = serviceProductRepository.save(new ServiceProduct(
                service,
                product,
                virtualTimeService.getCurrentVirtualTime()
        ));
        logEvent(EventType.SERVICE_PRODUCT_ASSIGNED, "SERVICE_PRODUCT", serviceProduct.id(), service.account().id(),
                "Producto " + product.code() + " asignado al servicio " + service.serviceCode() + ".");
        return serviceProduct;
    }

    /**
     * Cancela una asignacion producto-servicio.
     *
     * @param serviceId servicio propietario de la asignacion.
     * @param serviceProductId asignacion a cancelar.
     * @return asignacion actualizada.
     * @throws BrmServiceNotFoundException si el servicio no existe.
     * @throws ServiceProductNotFoundException si la asignacion no pertenece al servicio.
     * @throws AccountClosedException si la cuenta esta cerrada.
     */
    ServiceProduct cancelProduct(String serviceId, String serviceProductId) {
        var service = getService(serviceId);
        ensureAccountActive(service.account());
        var serviceProduct = serviceProductRepository.findById(serviceProductId)
                .filter(product -> product.service().id().equals(serviceId))
                .orElseThrow(() -> new ServiceProductNotFoundException("No existe un producto asignado con id " + serviceProductId + "."));
        serviceProduct.cancel(virtualTimeService.getCurrentVirtualTime());
        var saved = serviceProductRepository.save(serviceProduct);
        logEvent(EventType.SERVICE_PRODUCT_CANCELLED, "SERVICE_PRODUCT", saved.id(), service.account().id(),
                "Producto " + saved.product().code() + " cancelado del servicio " + service.serviceCode() + ".");
        return saved;
    }

    /**
     * Consulta un servicio por id.
     *
     * @param serviceId identificador de servicio.
     * @return servicio encontrado.
     * @throws BrmServiceNotFoundException si no existe.
     */
    @Transactional(readOnly = true)
    BrmService getService(String serviceId) {
        var service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new BrmServiceNotFoundException("No existe un servicio con id " + serviceId + "."));
        ensureAccountVisible(service.account());
        return service;
    }

    /**
     * Lista servicios de una cuenta.
     *
     * @param accountId cuenta consultada.
     * @return servicios ordenados por fecha de creacion.
     * @throws AccountNotFoundException si la cuenta no existe.
     */
    @Transactional(readOnly = true)
    List<BrmService> getServicesByAccount(String accountId) {
        getAccount(accountId);
        return serviceRepository.findByAccountIdOrderByCreatedAtAsc(accountId);
    }

    /**
     * Lista productos asignados a un servicio.
     *
     * @param serviceId servicio consultado.
     * @return asignaciones ordenadas por fecha.
     * @throws BrmServiceNotFoundException si el servicio no existe.
     */
    @Transactional(readOnly = true)
    List<ServiceProduct> getProductsByService(String serviceId) {
        getService(serviceId);
        return serviceProductRepository.findByServiceIdOrderByAssignedAtAsc(serviceId);
    }

    /**
     * Lista todos los servicios del sistema.
     *
     * @return servicios ordenados por fecha de creacion.
     */
    @Transactional(readOnly = true)
    List<BrmService> getAllServices() {
        return serviceRepository.findAll().stream()
                .filter(service -> userContextService.canAccess(service.account()))
                .sorted(Comparator.comparing(BrmService::createdAt))
                .toList();
    }

    /**
     * Lista el catalogo general de servicios.
     *
     * @return definiciones ordenadas por fecha.
     */
    @Transactional(readOnly = true)
    List<ServiceCatalog> getCatalogServices() {
        return serviceCatalogRepository.findAll().stream()
                .sorted(Comparator.comparing(ServiceCatalog::createdAt))
                .toList();
    }

    /**
     * Consulta una definicion general de servicio.
     *
     * @param catalogServiceId identificador de catalogo.
     * @return definicion encontrada.
     * @throws BrmServiceNotFoundException si no existe.
     */
    @Transactional(readOnly = true)
    ServiceCatalog getCatalogService(String catalogServiceId) {
        return serviceCatalogRepository.findById(catalogServiceId)
                .orElseThrow(() -> new BrmServiceNotFoundException("No existe un servicio general con id " + catalogServiceId + "."));
    }

    private Account getAccount(String accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("No existe una cuenta con id " + accountId + "."));
        ensureAccountVisible(account);
        return account;
    }

    private void ensureAccountVisible(Account account) {
        if (!userContextService.canAccess(account)) {
            throw new AccountNotFoundException("No existe una cuenta con id " + account.id() + ".");
        }
    }

    private void ensureAccountActive(Account account) {
        if (account.status() == AccountStatus.CLOSED) {
            throw new AccountClosedException("La cuenta esta cerrada y no permite operaciones de servicios.");
        }
    }

    private String normalizeServiceCode(String serviceCode, String accountId, ServiceType serviceType) {
        if (serviceCode != null && !serviceCode.isBlank()) {
            return serviceCode.trim().toUpperCase();
        }
        return (serviceType == null ? ServiceType.GENERIC : serviceType).name()
                + "-"
                + accountId
                + "-"
                + virtualTimeService.getCurrentVirtualTime().format(SERVICE_CODE_FORMAT);
    }

    private String generatedServiceCode(String accountId, ServiceType serviceType, String serviceId) {
        return (serviceType == null ? ServiceType.GENERIC : serviceType).name()
                + "-"
                + accountId
                + "-"
                + serviceId;
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("El nombre del servicio es obligatorio.");
        }
        return name.trim();
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String defaultServiceName(ServiceType serviceType) {
        return switch (serviceType == null ? ServiceType.GENERIC : serviceType) {
            case MOBILE -> "Servicio movil";
            case INTERNET -> "Servicio de internet";
            case TV -> "Servicio de television";
            case GENERIC -> "Servicio general";
        };
    }

    private void logEvent(EventType type, String entityType, String entityId, String accountId, String description) {
        eventRepository.save(new SystemEvent(
                type,
                entityType,
                entityId,
                accountId,
                description,
                virtualTimeService.getCurrentVirtualTime()
        ));
    }
}
