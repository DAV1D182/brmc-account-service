package com.brmc.account;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de auditoria del sistema.
 *
 * <p>Todos los modulos de negocio escriben eventos en {@code system_events_t} para mantener
 * trazabilidad consultable por cuenta y tipo de evento.</p>
 */
interface SystemEventRepository extends JpaRepository<SystemEvent, String> {

    /**
     * Lista eventos asociados a una cuenta.
     *
     * @param accountId identificador de cuenta.
     * @return eventos ordenados por fecha de creacion ascendente.
     */
    List<SystemEvent> findByAccountIdOrderByCreatedAtAsc(String accountId);

    /**
     * Lista eventos por tipo.
     *
     * @param type tipo de evento.
     * @return eventos ordenados por fecha de creacion ascendente.
     */
    List<SystemEvent> findByTypeOrderByCreatedAtAsc(EventType type);

    /**
     * Lista eventos de una cuenta filtrados por tipo.
     *
     * @param accountId identificador de cuenta.
     * @param type tipo de evento.
     * @return eventos coincidentes ordenados por fecha de creacion ascendente.
     */
    List<SystemEvent> findByAccountIdAndTypeOrderByCreatedAtAsc(String accountId, EventType type);
}
