package com.brmc.account;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio del catalogo general de servicios.
 */
interface ServiceCatalogRepository extends JpaRepository<ServiceCatalog, String> {

    /**
     * Lista definiciones por estado.
     *
     * @param status estado de catalogo.
     * @return definiciones ordenadas por fecha de creacion.
     */
    List<ServiceCatalog> findByStatusOrderByCreatedAtAsc(ServiceCatalogStatus status);
}
