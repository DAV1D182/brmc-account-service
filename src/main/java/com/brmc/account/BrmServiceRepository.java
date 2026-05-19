package com.brmc.account;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de servicios BRM asociados a cuentas.
 */
interface BrmServiceRepository extends JpaRepository<BrmService, String> {

    /**
     * Lista todos los servicios cargando la cuenta propietaria para construir respuestas sin
     * depender de Open Session in View.
     *
     * @return servicios con su cuenta asociada.
     */
    @Override
    @EntityGraph(attributePaths = "account")
    List<BrmService> findAll();

    /**
     * Verifica unicidad del codigo funcional del servicio.
     *
     * @param serviceCode codigo del servicio.
     * @return {@code true} si el codigo ya existe.
     */
    boolean existsByServiceCode(String serviceCode);

    /**
     * Busca un servicio por codigo funcional.
     *
     * @param serviceCode codigo del servicio.
     * @return servicio encontrado, si existe.
     */
    Optional<BrmService> findByServiceCode(String serviceCode);

    /**
     * Lista servicios de una cuenta.
     *
     * @param accountId identificador de cuenta.
     * @return servicios ordenados por fecha de creacion ascendente.
     */
    @EntityGraph(attributePaths = "account")
    List<BrmService> findByAccountIdOrderByCreatedAtAsc(String accountId);

    /**
     * Lista servicios por estado operativo.
     *
     * @param status estado solicitado.
     * @return servicios coincidentes ordenados por fecha de creacion ascendente.
     */
    @EntityGraph(attributePaths = "account")
    List<BrmService> findByStatusOrderByCreatedAtAsc(ServiceStatus status);
}
