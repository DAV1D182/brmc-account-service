package com.brmc.account;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de asignaciones entre servicios y productos.
 */
interface ServiceProductRepository extends JpaRepository<ServiceProduct, String> {

    /**
     * Verifica si un producto ya esta activo en un servicio.
     *
     * @param serviceId identificador del servicio.
     * @param productId identificador del producto.
     * @param status estado de la asignacion evaluada.
     * @return {@code true} si existe una asignacion con la combinacion indicada.
     */
    boolean existsByServiceIdAndProductIdAndStatus(String serviceId, String productId, ServiceProductStatus status);

    /**
     * Lista productos asignados a un servicio.
     *
     * @param serviceId identificador del servicio.
     * @return asignaciones ordenadas por fecha de asignacion ascendente.
     */
    List<ServiceProduct> findByServiceIdOrderByAssignedAtAsc(String serviceId);

    /**
     * Lista productos de un servicio filtrados por estado.
     *
     * @param serviceId identificador del servicio.
     * @param status estado de la asignacion.
     * @return asignaciones coincidentes ordenadas por fecha de asignacion ascendente.
     */
    List<ServiceProduct> findByServiceIdAndStatusOrderByAssignedAtAsc(String serviceId, ServiceProductStatus status);

    /**
     * Lista asignaciones por estado.
     *
     * @param status estado de la asignacion.
     * @return asignaciones ordenadas por fecha de asignacion ascendente.
     */
    List<ServiceProduct> findByStatusOrderByAssignedAtAsc(ServiceProductStatus status);
}
