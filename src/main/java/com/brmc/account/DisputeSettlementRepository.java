package com.brmc.account;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para settlements creados sobre disputas.
 */
interface DisputeSettlementRepository extends JpaRepository<DisputeSettlement, String> {

    /**
     * Lista settlements asociados a una disputa.
     *
     * @param disputeId identificador de la disputa.
     * @return settlements ordenados por fecha de creacion ascendente.
     */
    List<DisputeSettlement> findByDisputeIdOrderByCreatedAtAsc(String disputeId);

    /**
     * Lista settlements asociados a una cuenta.
     *
     * @param accountId identificador de cuenta.
     * @return settlements ordenados por fecha de creacion ascendente.
     */
    List<DisputeSettlement> findByAccountIdOrderByCreatedAtAsc(String accountId);
}
