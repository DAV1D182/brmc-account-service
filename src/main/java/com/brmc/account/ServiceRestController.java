package com.brmc.account;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST del modulo de servicios.
 *
 * <p>Expone servicios asociados a cuentas y operaciones sobre productos asignados. El controlador
 * mantiene rutas similares a recursos BRM, mientras {@link ServiceManagementService} concentra las
 * validaciones de cuenta, servicio y producto.</p>
 */
@RestController
@RequestMapping("/api")
class ServiceRestController {

    private final ServiceManagementService serviceManagementService;

    /**
     * Crea el controlador de servicios.
     *
     * @param serviceManagementService servicio de administracion de servicios.
     */
    ServiceRestController(ServiceManagementService serviceManagementService) {
        this.serviceManagementService = serviceManagementService;
    }

    /**
     * Lista servicios de una cuenta.
     *
     * @param accountId cuenta consultada.
     * @return servicios con productos asignados.
     */
    @GetMapping("/accounts/{accountId}/services")
    List<BrmServiceResponse> getAccountServices(@PathVariable String accountId) {
        return serviceManagementService.getServicesByAccount(accountId).stream()
                .map(service -> BrmServiceResponse.from(service, serviceManagementService.getProductsByService(service.id())))
                .toList();
    }

    /**
     * Crea un servicio para una cuenta.
     *
     * @param accountId cuenta propietaria.
     * @param request datos de servicio.
     * @return servicio creado sin productos asignados.
     */
    @PostMapping("/accounts/{accountId}/services")
    @ResponseStatus(HttpStatus.CREATED)
    BrmServiceResponse createService(
            @PathVariable String accountId,
            @Valid @RequestBody CreateServiceRequest request
    ) {
        var service = request.catalogServiceId() == null || request.catalogServiceId().isBlank()
                ? serviceManagementService.createService(accountId, request.serviceName(), request.serviceCode(), request.serviceType())
                : serviceManagementService.activateCatalogService(accountId, request.catalogServiceId());
        return BrmServiceResponse.from(service, List.of());
    }

    /**
     * Activa una definicion del catalogo en una cuenta.
     *
     * @param accountId cuenta destino.
     * @param catalogServiceId definicion general de servicio.
     * @return servicio activo de cuenta.
     */
    @PostMapping("/accounts/{accountId}/services/catalog/{catalogServiceId}/activate")
    @ResponseStatus(HttpStatus.CREATED)
    BrmServiceResponse activateCatalogService(
            @PathVariable String accountId,
            @PathVariable String catalogServiceId
    ) {
        var service = serviceManagementService.activateCatalogService(accountId, catalogServiceId);
        return BrmServiceResponse.from(service, List.of());
    }

    /**
     * Lista servicios creados con filtros opcionales.
     *
     * @param accountId cuenta opcional para limitar la consulta.
     * @param serviceType tipo de servicio opcional.
     * @param status estado operativo opcional.
     * @return servicios con sus productos asignados.
     */
    @GetMapping("/services")
    List<BrmServiceResponse> getServices(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) ServiceType serviceType,
            @RequestParam(required = false) ServiceStatus status
    ) {
        var services = accountId == null || accountId.isBlank()
                ? serviceManagementService.getAllServices()
                : serviceManagementService.getServicesByAccount(accountId);
        return services.stream()
                .filter(service -> serviceType == null || service.serviceType() == serviceType)
                .filter(service -> status == null || service.status() == status)
                .map(service -> BrmServiceResponse.from(service, serviceManagementService.getProductsByService(service.id())))
                .toList();
    }

    /**
     * Consulta un servicio.
     *
     * @param serviceId identificador de servicio.
     * @return servicio con productos asignados.
     */
    @GetMapping("/services/{serviceId}")
    BrmServiceResponse getService(@PathVariable String serviceId) {
        var service = serviceManagementService.getService(serviceId);
        return BrmServiceResponse.from(service, serviceManagementService.getProductsByService(service.id()));
    }

    /**
     * Actualiza un servicio.
     *
     * @param serviceId servicio a editar.
     * @param request datos editables.
     * @return servicio actualizado con productos asociados.
     */
    @PutMapping("/services/{serviceId}")
    BrmServiceResponse updateService(
            @PathVariable String serviceId,
            @Valid @RequestBody CreateServiceRequest request
    ) {
        var service = serviceManagementService.updateService(
                serviceId,
                request.serviceName(),
                request.serviceCode(),
                request.serviceType()
        );
        return BrmServiceResponse.from(service, serviceManagementService.getProductsByService(service.id()));
    }

    /**
     * Lista el catalogo general de servicios.
     *
     * @return definiciones creadas.
     */
    @GetMapping("/service-catalog")
    List<ServiceCatalogResponse> getServiceCatalog() {
        return serviceManagementService.getCatalogServices().stream()
                .map(ServiceCatalogResponse::from)
                .toList();
    }

    /**
     * Consulta una definicion general de servicio.
     *
     * @param catalogServiceId definicion consultada.
     * @return datos de catalogo.
     */
    @GetMapping("/service-catalog/{catalogServiceId}")
    ServiceCatalogResponse getServiceCatalogItem(@PathVariable String catalogServiceId) {
        return ServiceCatalogResponse.from(serviceManagementService.getCatalogService(catalogServiceId));
    }

    /**
     * Crea una definicion general de servicio.
     *
     * @param request datos de catalogo.
     * @return definicion creada.
     */
    @PostMapping("/service-catalog")
    @ResponseStatus(HttpStatus.CREATED)
    ServiceCatalogResponse createServiceCatalog(@Valid @RequestBody ServiceCatalogRequest request) {
        return ServiceCatalogResponse.from(serviceManagementService.createCatalogService(
                request.name(),
                request.serviceType(),
                request.description(),
                request.status()
        ));
    }

    /**
     * Actualiza una definicion general de servicio.
     *
     * @param catalogServiceId definicion a editar.
     * @param request datos actualizados.
     * @return definicion actualizada.
     */
    @PutMapping("/service-catalog/{catalogServiceId}")
    ServiceCatalogResponse updateServiceCatalog(
            @PathVariable String catalogServiceId,
            @Valid @RequestBody ServiceCatalogRequest request
    ) {
        return ServiceCatalogResponse.from(serviceManagementService.updateCatalogService(
                catalogServiceId,
                request.name(),
                request.serviceType(),
                request.description(),
                request.status()
        ));
    }

    /**
     * Suspende un servicio.
     *
     * @param serviceId servicio a suspender.
     * @return servicio actualizado.
     */
    @PostMapping("/services/{serviceId}/suspend")
    BrmServiceResponse suspendService(@PathVariable String serviceId) {
        var service = serviceManagementService.suspendService(serviceId);
        return BrmServiceResponse.from(service, serviceManagementService.getProductsByService(service.id()));
    }

    /**
     * Reactiva un servicio.
     *
     * @param serviceId servicio a reactivar.
     * @return servicio actualizado.
     */
    @PostMapping("/services/{serviceId}/reactivate")
    BrmServiceResponse reactivateService(@PathVariable String serviceId) {
        var service = serviceManagementService.reactivateService(serviceId);
        return BrmServiceResponse.from(service, serviceManagementService.getProductsByService(service.id()));
    }

    /**
     * Termina un servicio.
     *
     * @param serviceId servicio a terminar.
     * @return servicio actualizado.
     */
    @PostMapping("/services/{serviceId}/terminate")
    BrmServiceResponse terminateService(@PathVariable String serviceId) {
        var service = serviceManagementService.terminateService(serviceId);
        return BrmServiceResponse.from(service, serviceManagementService.getProductsByService(service.id()));
    }

    /**
     * Asigna un producto a un servicio.
     *
     * @param serviceId servicio receptor.
     * @param productId producto del catalogo.
     * @return asignacion creada.
     */
    @PostMapping("/services/{serviceId}/products/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    ServiceProductResponse assignProduct(@PathVariable String serviceId, @PathVariable String productId) {
        return ServiceProductResponse.from(serviceManagementService.assignProduct(serviceId, productId));
    }

    /**
     * Cancela un producto asignado a un servicio.
     *
     * @param serviceId servicio propietario.
     * @param serviceProductId asignacion a cancelar.
     * @return asignacion actualizada.
     */
    @PostMapping("/services/{serviceId}/products/{serviceProductId}/cancel")
    ServiceProductResponse cancelProduct(@PathVariable String serviceId, @PathVariable String serviceProductId) {
        return ServiceProductResponse.from(serviceManagementService.cancelProduct(serviceId, serviceProductId));
    }

    /**
     * Solicitud para crear un servicio.
     *
     * @param serviceCode codigo opcional; el sistema genera uno si falta.
     * @param serviceType tipo opcional; GENERIC si falta.
     */
    record CreateServiceRequest(
            @Size(max = 160, message = "serviceName no puede superar 160 caracteres")
            String serviceName,

            @Size(max = 80, message = "serviceCode no puede superar 80 caracteres")
            String serviceCode,
            ServiceType serviceType,

            String catalogServiceId
    ) {
    }

    /**
     * Solicitud para crear o actualizar una definicion general de servicio.
     *
     * @param name nombre visible obligatorio.
     * @param serviceType tipo funcional.
     * @param description descripcion opcional.
     * @param status estado de disponibilidad.
     */
    record ServiceCatalogRequest(
            @jakarta.validation.constraints.NotBlank(message = "name es obligatorio")
            @Size(max = 160, message = "name no puede superar 160 caracteres")
            String name,

            ServiceType serviceType,

            @Size(max = 500, message = "description no puede superar 500 caracteres")
            String description,

            ServiceCatalogStatus status
    ) {
    }
}
