package com.brmc.account;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de items de inventario persistidos en {@code inventory_t}.
 */
interface InventoryItemRepository extends JpaRepository<InventoryItem, String> {

    /**
     * Lista items por disponibilidad.
     *
     * @param available disponibilidad solicitada.
     * @return items ordenados por nombre.
     */
    List<InventoryItem> findByAvailableOrderByNameAsc(Boolean available);
}
